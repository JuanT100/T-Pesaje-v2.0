package com.tnova.t_pesaje.features.security

/**
 * Estados posibles de la renovación de sesión.
 */
enum class SessionRefreshStatus {
    SUCCESS,
    TEMPORARY_FAILURE,
    INVALID_SESSION
}
