package com.tnova.t_pesaje.features.security

import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Ejecuta solicitudes HTTPS que requieren autenticación.
 */
class AuthenticatedApiClient(
    private val sessionStorage: SessionStorage,
    private val sessionManager: SessionManager
) {

    /**
     * Ejecuta una solicitud autenticada.
     *
     * Renueva el Access Token cuando corresponde.
     * Si el Backend devuelve 401, intenta renovar el token
     * y repetir la solicitud una sola vez.
     */
    fun execute(
        method: String,
        endpoint: String,
        body: String? = null
    ): AuthenticatedApiResponse {

        var accessToken = sessionStorage.getAccessToken()
            ?: return AuthenticatedApiResponse(
                success = false,
                statusCode = 401,
                body = ""
            )

        // Renueva el Access Token cuando sea necesario.
        if (sessionManager.shouldRefreshToken()) {
            when (sessionManager.refreshSession().status) {

                SessionRefreshStatus.SUCCESS -> {
                    accessToken = sessionStorage.getAccessToken()
                        ?: return AuthenticatedApiResponse(
                            success = false,
                            statusCode = 401,
                            body = ""
                        )
                }

                SessionRefreshStatus.TEMPORARY_FAILURE -> {
                    // Se conserva el token actual y se intenta
                    // la solicitud.
                }

                SessionRefreshStatus.INVALID_SESSION -> {
                    return AuthenticatedApiResponse(
                        success = false,
                        statusCode = 401,
                        body = ""
                    )
                }
            }
        }

        var response = executeRequest(
            method = method,
            endpoint = endpoint,
            body = body,
            accessToken = accessToken
        )

        // Si el Backend rechaza el token, intenta renovarlo.
        if (response.statusCode == 401) {
            when (sessionManager.refreshSession().status) {

                SessionRefreshStatus.SUCCESS -> {
                    accessToken = sessionStorage.getAccessToken()
                        ?: return response

                    // Reintenta la solicitud una sola vez.
                    response = executeRequest(
                        method = method,
                        endpoint = endpoint,
                        body = body,
                        accessToken = accessToken
                    )
                }

                SessionRefreshStatus.TEMPORARY_FAILURE -> {
                    return AuthenticatedApiResponse(
                        success = false,
                        statusCode = 0,
                        body = ""
                    )
                }

                SessionRefreshStatus.INVALID_SESSION -> {
                    return response
                }
            }
        }

        return response
    }

    /**
     * Resuelve la URL y comprueba que la solicitud se dirija
     * exclusivamente al servidor HTTPS configurado.
     *
     * Las rutas relativas utilizan ApiConfiguration.BASE_URL.
     * Las URL absolutas solo se aceptan si coinciden con el
     * protocolo, host y puerto del servidor configurado.
     */
    private fun resolveUrl(endpoint: String): URL {
        require(endpoint.isNotBlank()) {
            "La ruta de la solicitud está vacía."
        }

        val baseUrl = URL(ApiConfiguration.BASE_URL)

        require(
            baseUrl.protocol.equals("https", ignoreCase = true)
        ) {
            "El servidor debe utilizar HTTPS."
        }

        val completeUrl = if (endpoint.contains("://")) {
            URL(endpoint)
        } else {
            URL(baseUrl, endpoint.trimStart('/'))
        }

        val sameProtocol = completeUrl.protocol.equals(
            "https",
            ignoreCase = true
        )

        val sameHost = completeUrl.host.equals(
            baseUrl.host,
            ignoreCase = true
        )

        val completePort = if (completeUrl.port != -1) {
            completeUrl.port
        } else {
            completeUrl.defaultPort
        }

        val basePort = if (baseUrl.port != -1) {
            baseUrl.port
        } else {
            baseUrl.defaultPort
        }

        require(
            sameProtocol &&
                    sameHost &&
                    completePort == basePort &&
                    completeUrl.userInfo == null
        ) {
            "La URL no pertenece al servidor HTTPS configurado."
        }

        return completeUrl
    }

    /**
     * Ejecuta la solicitud HTTPS con el Access Token indicado.
     */
    private fun executeRequest(
        method: String,
        endpoint: String,
        body: String?,
        accessToken: String
    ): AuthenticatedApiResponse {

        val connection = try {
            resolveUrl(endpoint)
                .openConnection() as HttpsURLConnection
        } catch (_: Exception) {
            return AuthenticatedApiResponse(
                success = false,
                statusCode = 0,
                body = ""
            )
        }

        return try {
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doInput = true

            // Envía el JWT únicamente al servidor validado.
            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            if (body != null) {
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                )

                connection.outputStream.use { output ->
                    output.write(body.toByteArray(Charsets.UTF_8))
                }
            }

            val responseCode = connection.responseCode

            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = inputStream?.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: ""

            AuthenticatedApiResponse(
                success = responseCode in 200..299,
                statusCode = responseCode,
                body = responseBody
            )

        } catch (_: Exception) {
            // Código 0: error de comunicación.
            // No se exponen detalles técnicos.
            AuthenticatedApiResponse(
                success = false,
                statusCode = 0,
                body = ""
            )
        } finally {
            connection.disconnect()
        }
    }
}
