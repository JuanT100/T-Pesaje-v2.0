package com.tnova.t_pesaje.features.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.tnova.t_pesaje.features.authentication.LoginResult
import com.tnova.t_pesaje.features.security.AuthenticatedApiClient
import com.tnova.t_pesaje.features.security.LogoutApi
import com.tnova.t_pesaje.features.security.SessionConfigurationApi
import com.tnova.t_pesaje.features.security.SessionInactivityManager
import com.tnova.t_pesaje.features.security.SessionManager
import com.tnova.t_pesaje.features.security.SessionStorage
import com.tnova.t_pesaje.screens.authentication.LoginScreen
import com.tnova.t_pesaje.screens.navigation.MainMenuItem
import com.tnova.t_pesaje.screens.navigation.MainMenuOption
import com.tnova.t_pesaje.screens.navigation.MainScreen
import com.tnova.t_pesaje.ui.theme.TPesajeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Actividad principal de T-Pesaje.
 *
 * Coordina autenticación, sesión y navegación principal.
 */
class MainActivity : ComponentActivity() {

    private val sessionStorage by lazy {
        SessionStorage(applicationContext)
    }

    private val sessionManager by lazy {
        SessionManager(sessionStorage)
    }

    private val authenticatedApiClient by lazy {
        AuthenticatedApiClient(
            sessionStorage = sessionStorage,
            sessionManager = sessionManager
        )
    }

    private val inactivityManager by lazy {
        SessionInactivityManager()
    }

    // Estado de autenticación durante la ejecución del proceso.
    private var authenticated by mutableStateOf(false)

    // Usuario autenticado actualmente.
    private var userId by mutableStateOf<Int?>(null)

    // Nombre completo del usuario autenticado.
    private var fullName by mutableStateOf("")

    // Opciones disponibles para el usuario actual.
    private var menuItems by mutableStateOf<List<MainMenuItem>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            TPesajeTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    if (!authenticated) {

                        // Muestra Login al iniciar una nueva ejecución
                        LoginScreen(
                            onLogin = { result ->

                                if (
                                    result.success &&
                                    result.userId != null
                                ) {
                                    startSession(result)
                                }
                            }
                        )

                    } else {

                        // Muestra la pantalla principal después de una autenticación correcta.
                        MainScreen(
                            modifier = Modifier.padding(innerPadding),
                            userId = userId,
                            fullName = fullName,
                            menuItems = menuItems,
                            onUserActivity = {
                                inactivityManager.registerActivity()
                            },
                            onLogout = {
                                logout()
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Se ejecuta cuando la actividad vuelve a estar visible.
     *
     * Verifica el tiempo de inactividad antes de continuar con la sesión existente.
     */
    override fun onResume() {
        super.onResume()

        if (!authenticated) return

        if (inactivityManager.isInactive()) {
            logout()
        } else {
            inactivityManager.registerActivity()
        }
    }

    /**
     * Inicia la sesión de aplicación guardando los tokens después de un Login correcto.
     */
    private fun startSession(result: LoginResult)
    {
        val userId = result.userId ?: return
        val accessToken = result.accessToken ?: return
        val refreshToken = result.refreshToken ?: return

        // Guarda la sesión localmente.
        sessionStorage.saveSession(
            userId = userId,
            username = result.username.orEmpty(),
            fullName = result.fullName.orEmpty(),
            mustChangePassword = result.mustChangePassword,
            accessToken = accessToken,
            refreshToken = refreshToken
        )

        this.userId = userId
        this.fullName = result.fullName.orEmpty()

        // El usuario comienza una nueva actividad de sesión.
        inactivityManager.registerActivity()

        // Inicialmente se muestran las opciones actualmente habilitadas para esta etapa.
        menuItems = getMenuByPermissions(userId)

        authenticated = true

        // Obtiene la configuración de inactividad desde el Backend.
        loadSessionConfiguration()
    }

    /**
     * Obtiene las opciones de menú disponibles para el usuario.
     */
    private fun getMenuByPermissions(
        userId: Int
    ): List<MainMenuItem> {

        return listOf(
            MainMenuItem(
                option = MainMenuOption.WEIGHING,
                label = "Pesaje"
            ),
            MainMenuItem(
                option = MainMenuOption.HISTORY,
                label = "Historial"
            )
        )
    }

    /**
     * Obtiene desde el Backend el tiempo de inactividad configurado.
     */
    private fun loadSessionConfiguration() {

        lifecycleScope.launch {

            val inactivityMinutes =
                withContext(Dispatchers.IO) {

                    SessionConfigurationApi.getInactivityMinutes(
                        authenticatedApiClient
                    )
                }

            if (inactivityMinutes != null) {

                inactivityManager.setTimeout(
                    inactivityMinutes
                )
            }
        }
    }

    /**
     * Cierra la sesión actual.
     *
     * Primero intenta revocar el Refresh Token en el Backend.
     * Después elimina siempre la sesión almacenada localmente.
     */
    private fun logout() {

        if (!authenticated) return

        val accessToken =
            sessionStorage.getAccessToken()

        val refreshToken =
            sessionStorage.getRefreshToken()

        lifecycleScope.launch {

            withContext(Dispatchers.IO) {

                if (
                    accessToken != null &&
                    refreshToken != null
                ) {
                    LogoutApi.logout(
                        accessToken = accessToken,
                        refreshToken = refreshToken
                    )
                }

                // Elimina la sesión local
                sessionStorage.clearSession()
            }

            userId = null
            menuItems = emptyList()
            authenticated = false

            inactivityManager.registerActivity()
        }
    }
}
