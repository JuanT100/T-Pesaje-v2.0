package com.tnova.t_pesaje.features.security

import org.json.JSONObject

/**
 * Obtiene la configuración de sesión desde la API.
 */
object SessionConfigurationApi {

    /**
     * Consulta el tiempo de inactividad configurado.
     */
    fun getInactivityMinutes(
        client: AuthenticatedApiClient
    ): Int? {

        val response =
            client.execute(
                method = "GET",
                endpoint = "/api/configuration/session"
            )

        if (!response.success)
            return null

        return try {

            val json =
                JSONObject(response.body)

            json.optInt(
                "inactivityMinutes",
                0
            ).takeIf { it > 0 }

        } catch (_: Exception) {

            null
        }
    }
}