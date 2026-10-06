
package com.tnova.t_pesaje.features.security

import android.util.Base64
import org.json.JSONObject

/**
 * Gestiona la renovación de los tokens de sesión.
 */
class SessionManager(
    private val sessionStorage: SessionStorage
) {

    /**
     * Renueva los tokens almacenados.
     */
    fun refreshSession(): SessionRefreshResult {

        val refreshToken =
            sessionStorage.getRefreshToken()
                ?: return SessionRefreshResult(
                    status = SessionRefreshStatus.INVALID_SESSION
                )

        val result = TokenRefreshApi.refresh(refreshToken)

        if (result.success) {

            val accessToken =
                result.accessToken
                    ?: return SessionRefreshResult(
                        status = SessionRefreshStatus.INVALID_SESSION
                    )

            val newRefreshToken =
                result.refreshToken
                    ?: return SessionRefreshResult(
                        status = SessionRefreshStatus.INVALID_SESSION
                    )

            sessionStorage.updateTokens(
                accessToken = accessToken,
                refreshToken = newRefreshToken
            )

            return SessionRefreshResult(
                status = SessionRefreshStatus.SUCCESS
            )
        }

        if (result.temporaryFailure) {

            // La comunicación falló temporalmente.
            // La sesión local no se elimina.
            return SessionRefreshResult(
                status = SessionRefreshStatus.TEMPORARY_FAILURE,
                message = result.message
            )
        }

        // El Refresh Token ya no puede utilizarse.
        return SessionRefreshResult(
            status = SessionRefreshStatus.INVALID_SESSION,
            message = result.message
        )
    }

    /**
     * Determina si el Access Token debe renovarse.
     *
     * Retorna true cuando el token está próximo a vencer
     * o ya ha expirado.
     */
    fun shouldRefreshToken(): Boolean {

        val accessToken =
            sessionStorage.getAccessToken()
                ?: return true

        return try {

            val parts = accessToken.split(".")

            if (parts.size != 3) {
                return true
            }

            val payload = String(
                Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or
                            Base64.NO_WRAP or
                            Base64.NO_PADDING
                ),
                Charsets.UTF_8
            )

            val json = JSONObject(payload)

            val expiration = json.getLong("exp")
            val currentTime = System.currentTimeMillis() / 1000
            val refreshMargin = 60

            expiration - currentTime <= refreshMargin

        } catch (_: Exception) {

            // Si no es posible validar el token,
            // se solicita su renovación.
            true
        }
    }
}
