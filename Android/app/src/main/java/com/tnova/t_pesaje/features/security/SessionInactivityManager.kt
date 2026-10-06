package com.tnova.t_pesaje.features.security

import android.os.SystemClock

/**
 * Controla el tiempo de inactividad de la sesión.
 */
class SessionInactivityManager {

    // Valor inicial utilizado mientras se obtiene
    // la configuración desde el Backend.
    private var timeoutMinutes: Int = 15

    // Momento de la última interacción registrada.
    private var lastActivity =
        SystemClock.elapsedRealtime()

    /**
     * Actualiza la configuración de inactividad.
     */
    fun setTimeout(minutes: Int) {

        if (minutes > 0) {
            timeoutMinutes = minutes
        }
    }

    /**
     * Registra una interacción del usuario.
     */
    fun registerActivity() {

        lastActivity =
            SystemClock.elapsedRealtime()
    }

    /**
     * Determina si la sesión está inactiva.
     */
    fun isInactive(): Boolean {

        val elapsed =
            SystemClock.elapsedRealtime() -
                    lastActivity

        val timeoutMillis =
            timeoutMinutes * 60_000L

        return elapsed >= timeoutMillis
    }
}
