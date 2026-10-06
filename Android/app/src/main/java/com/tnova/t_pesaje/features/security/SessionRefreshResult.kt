package com.tnova.t_pesaje.features.security

/**
 * Representa el resultado de la renovación de sesión.
 */
data class SessionRefreshResult(
    val status: SessionRefreshStatus,
    val message: String = ""
)
