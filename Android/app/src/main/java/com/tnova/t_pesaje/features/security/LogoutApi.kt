package com.tnova.t_pesaje.features.security

import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Cliente utilizado para cerrar la sesión.
 */
object LogoutApi {

    fun logout(
        accessToken: String,
        refreshToken: String
    ): Boolean {

        val url =
            URL("${ApiConfiguration.BASE_URL}/api/auth/logout")

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

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            // Construye la solicitud de Logout.
            val requestJson =
                JSONObject().apply {
                    put("refreshToken", refreshToken)
                }

            // Envía la solicitud.
            connection.outputStream.use { output ->
                output.write(
                    requestJson
                        .toString()
                        .toByteArray(Charsets.UTF_8)
                )
            }

            connection.responseCode == 200

        } catch (_: Exception) {

            false

        } finally {

            connection.disconnect()
        }
    }
}
