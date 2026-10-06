package com.tnova.t_pesaje.features.security

/**
 * Representa el resultado recibido del endpoint de renovación.
 */
data class TokenRefreshResult(
    val success: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val message: String,
    val temporaryFailure: Boolean = false
)
