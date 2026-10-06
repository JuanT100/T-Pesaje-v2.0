package com.tnova.t_pesaje.features.security

/**
 * Representa la respuesta de una solicitud HTTP autenticada.
 *
 * statusCode = 0 representa un error de comunicación.
 */
data class AuthenticatedApiResponse(
    val success: Boolean,
    val statusCode: Int,
    val body: String
)
