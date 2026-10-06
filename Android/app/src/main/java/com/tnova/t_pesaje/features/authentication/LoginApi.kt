package com.tnova.t_pesaje.features.authentication

import com.tnova.t_pesaje.features.security.ApiConfiguration
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException

// Cliente utilizado para comunicarse con la API de autenticación.
// No contiene acceso directo a SQL Server.
object LoginApi {

    // Ejecuta el proceso de autenticación contra la API.
    fun login(
        username: String,
        password: String
    ): LoginResult {

        val url =
            URL("${ApiConfiguration.BASE_URL}/api/auth/login")

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

            // Construye la solicitud de autenticación.
            val requestJson = JSONObject().apply {
                put("username", username)
                put("password", password)
            }

            // Envía la solicitud mediante HTTPS.
            connection.outputStream.use { output ->
                output.write(
                    requestJson.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val responseCode = connection.responseCode

            // Selecciona el flujo según el código HTTP.
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = inputStream?.use { stream ->
                BufferedReader(
                    InputStreamReader(
                        stream,
                        Charsets.UTF_8
                    )
                ).use { reader ->
                    reader.readText()
                }
            } ?: ""

            // Procesa una autenticación correcta.
            if (responseCode == 200) {

                val json = JSONObject(responseText)

                LoginResult(
                    success = json.optBoolean(
                        "success",
                        false
                    ),
                    userId = json.optInt("userId")
                        .takeIf { json.has("userId") && !json.isNull("userId") },
                    username = json.optString("username")
                        .takeIf { it.isNotEmpty() },
                    fullName = json.optString("fullName")
                        .takeIf { it.isNotEmpty() },
                    mustChangePassword = json.optBoolean(
                        "mustChangePassword",
                        false
                    ),
                    accessToken = json.optString("token")
                        .takeIf { it.isNotEmpty() },
                    refreshToken = json.optString("refreshToken")
                        .takeIf { it.isNotEmpty() },
                    message = json.optString(
                        "message",
                        "Autenticación correcta."
                    )
                )

            } else {

                // Obtiene el mensaje controlado enviado por la API.
                val message = try {

                    JSONObject(responseText).optString(
                        "message",
                        "No fue posible validar el acceso."
                    ).takeIf { it.isNotEmpty() }
                        ?: "No fue posible validar el acceso."

                } catch (_: Exception) {

                    when (responseCode) {
                        400 -> "Los datos de acceso no son válidos."
                        401 -> "No fue posible validar las credenciales."
                        503 -> "No fue posible validar el acceso."
                        else -> "No fue posible procesar el acceso."
                    }
                }

                LoginResult(
                    success = false,
                    message = message
                )
            }

        } catch (_: SocketTimeoutException) {

            LoginResult(
                success = false,
                message = "Tiempo de espera agotado."
            )

        } catch (_: SSLHandshakeException) {

            LoginResult(
                success = false,
                message = "No fue posible establecer una conexión HTTPS segura."
            )

        } catch (_: ConnectException) {

            LoginResult(
                success = false,
                message = "No fue posible conectar con la API."
            )

        } catch (_: Exception) {

            LoginResult(
                success = false,
                message = "No fue posible procesar el acceso."
            )

        } finally {

            // Cierra siempre la conexión.
            connection.disconnect()
        }
    }
}
