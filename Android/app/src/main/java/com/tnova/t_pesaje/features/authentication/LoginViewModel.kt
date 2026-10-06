package com.tnova.t_pesaje.features.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gestiona el proceso de autenticación de la pantalla de inicio de sesión.
 *
 * No gestiona los permisos efectivos ni establece la sesión global.
 * Estas responsabilidades permanecen en MainViewModel.
 */
class LoginViewModel : ViewModel() {

    var message by mutableStateOf("")
        private set

    var isValidating by mutableStateOf(false)
        private set

    var loginResult by mutableStateOf<LoginResult?>(null)
        private set

    /**
     * Limpia el mensaje cuando el usuario modifica sus credenciales.
     */
    fun onCredentialsChanged() {
        message = ""
    }

    /**
     * Valida las credenciales y ejecuta la autenticación.
     */
    fun login(
        username: String,
        password: String
    ) {
        if (isValidating) return

        if (username.isBlank()) {
            message = "Ingrese el nombre de usuario."
            return
        }

        if (password.isBlank()) {
            message = "Ingrese la contraseña."
            return
        }

        isValidating = true
        message = ""
        loginResult = null

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    LoginApi.login(
                        username = username.trim(),
                        password = password
                    )
                }

                if (result.success) {
                    // La actividad principal continuará con la
                    // consulta de permisos y el establecimiento de la sesión.
                    loginResult = result
                } else {
                    // No se revela si el usuario existe,
                    // si la contraseña es incorrecta ni
                    // el detalle interno devuelto por el Backend.
                    message =
                        "No se pudo iniciar sesión. Verifique sus credenciales e inténtelo nuevamente."
                }
            } catch (_: Exception) {
                // No se muestra el detalle de la excepción.
                message =
                    "No se pudo conectar con el servicio. Inténtelo nuevamente."
            } finally {
                isValidating = false
            }
        }
    }

    /**
     * Consume el resultado después de notificarlo a la actividad principal.
     */
    fun consumeLoginResult() {
        loginResult = null
    }
}
