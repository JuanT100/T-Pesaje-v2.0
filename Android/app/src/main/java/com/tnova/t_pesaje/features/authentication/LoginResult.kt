package com.tnova.t_pesaje.features.authentication

// Resultado del proceso de autenticación.
data class LoginResult(
    val success: Boolean,
    val userId: Int? = null,
    val username: String? = null,
    val fullName: String? = null,
    val mustChangePassword: Boolean = false,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val message: String
)
