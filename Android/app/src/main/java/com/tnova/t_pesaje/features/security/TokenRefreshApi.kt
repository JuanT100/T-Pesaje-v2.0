package com.tnova.t_pesaje.features.security

import org.json.JSONObject
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Cliente utilizado para renovar los tokens de sesión.
 */
object TokenRefreshApi {

    /**
     * Renueva el Access Token utilizando el Refresh Token.
     */
    fun refresh(
        refreshToken: String
    ): TokenRefreshResult {

        val url =
            URL("${ApiConfiguration.BASE_URL}/api/auth/refresh")

        val connection =
            url.openConnection() as HttpsURLConnection

        return try {

            connection.requestMethod = "POST"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doInput = true
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json; charset=UTF-8"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            val requestJson = JSONObject().apply {
                put("refreshToken", refreshToken)
            }

            connection.outputStream.use { output ->
                output.write(
                    requestJson.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val responseCode = connection.responseCode

            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = inputStream?.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: ""

            if (responseCode == 200) {

                val json = JSONObject(responseText)

                TokenRefreshResult(
                    success = json.optBoolean("success", false),
                    accessToken = json.optString("token")
                        .takeIf { it.isNotEmpty() },
                    refreshToken = json.optString("refreshToken")
                        .takeIf { it.isNotEmpty() },
                    message = json.optString(
                        "message",
                        "Tokens renovados correctamente."
                    )
                )

            } else {

                val message = try {
                    JSONObject(responseText).optString(
                        "message",
                        "No fue posible renovar la sesión."
                    )
                } catch (_: Exception) {
                    "No fue posible renovar la sesión."
                }

                TokenRefreshResult(
                    success = false,
                    message = message,

                    // Los errores 5xx se consideran temporales.
                    temporaryFailure = responseCode >= 500
                )
            }

        } catch (_: IOException) {

            // La comunicación no está disponible.
            // La sesión no se invalida.
            TokenRefreshResult(
                success = false,
                message = "No fue posible renovar la sesión.",
                temporaryFailure = true
            )

        } catch (_: Exception) {

            TokenRefreshResult(
                success = false,
                message = "No fue posible renovar la sesión."
            )

        } finally {
            connection.disconnect()
        }
    }
}
