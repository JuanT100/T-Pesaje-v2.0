package com.tnova.t_pesaje.screens.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnova.t_pesaje.R
import com.tnova.t_pesaje.features.authentication.LoginResult
import com.tnova.t_pesaje.features.authentication.LoginViewModel

/**
 * Pantalla de autenticación de T-Pesaje.
 *
 * Presenta el formulario y delega el proceso de autenticación
 * a LoginViewModel. Los mensajes no exponen detalles técnicos.
 */
@Composable
fun LoginScreen(
    onLogin: (LoginResult) -> Unit,
    externalMessage: String = ""
) {
    val loginViewModel: LoginViewModel = viewModel()

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    // Permite ocultar un mensaje externo cuando el usuario
    // comienza a modificar sus credenciales.
    var hideExternalMessage by remember(externalMessage) {
        mutableStateOf(false)
    }

    // Notifica el resultado exitoso al flujo de autenticación
    // existente y consume el resultado para evitar reutilizarlo.
    LaunchedEffect(loginViewModel.loginResult) {
        val result = loginViewModel.loginResult

        if (result != null) {
            onLogin(result)
            loginViewModel.consumeLoginResult()
        }
    }

    // El mensaje externo tiene prioridad mientras no se descarte.
    val displayedMessage = if (
        externalMessage.isNotBlank() &&
        !hideExternalMessage
    ) {
        externalMessage
    } else {
        loginViewModel.message
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logotipo de T-Pesaje.
        Image(
            painter = painterResource(id = R.drawable.tpesaje_logo),
            contentDescription = "Logotipo de T-Pesaje",
            modifier = Modifier.size(120.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                loginViewModel.onCredentialsChanged()
                hideExternalMessage = true
            },
            label = {
                Text("Nombre de usuario")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loginViewModel.isValidating
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                loginViewModel.onCredentialsChanged()
                hideExternalMessage = true
            },
            label = {
                Text("Contraseña")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !loginViewModel.isValidating
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                hideExternalMessage = true

                loginViewModel.login(
                    username = username,
                    password = password
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loginViewModel.isValidating
        ) {
            Text("Ingresar")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (loginViewModel.isValidating) {
            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text("Validando...")
        }

        if (displayedMessage.isNotBlank()) {
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = displayedMessage
            )
        }
    }
}
