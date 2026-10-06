package com.tnova.t_pesaje.screens.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Opciones disponibles en el menú principal.
 */
enum class MainMenuOption {
    WEIGHING,   // Peso
    HISTORY     // Historial
}

/**
 * Representa una opción visible del menú.
 */
data class MainMenuItem(
    val option: MainMenuOption,
    val label: String
)

/**
 * Pantalla principal de trabajo de T-Pesaje.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userId: Int?,
    fullName: String,
    menuItems: List<MainMenuItem>,
    onUserActivity: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember {
        mutableStateOf(true)
    }

    var selectedOption by remember {
        mutableStateOf<MainMenuOption?>(null)
    }

    val menuWidth by animateDpAsState(
        targetValue =
            if (menuExpanded) {
                220.dp
            } else {
                64.dp
            },
        label = "menuWidth"
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {

                // Registra las interacciones del usuario para el control de inactividad.
                awaitPointerEventScope {

                    while (true) {
                        awaitPointerEvent(
                            PointerEventPass.Final
                        )
                        onUserActivity()
                    }
                }
            }
    ) {

        Surface(
            modifier = Modifier
                .width(menuWidth)
                .fillMaxHeight(),
            tonalElevation = 2.dp
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                // Encabezado del menú.
                if (menuExpanded) {

                    Text(
                        text = "T-Pesaje",
                        style =
                            MaterialTheme.typography.titleLarge,
                        modifier =
                            Modifier.padding(
                                top = 8 .dp,
                                bottom = 4.dp
                            )
                    )

                    Text(
                        text = fullName,
                        style =
                            MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier.padding(
                                bottom = 8.dp
                            )
                    )

                } else {

                    Text(
                        text = "T",
                        style =
                            MaterialTheme.typography.titleLarge,
                        modifier =
                            Modifier.padding(
                                vertical = 8.dp
                            )
                    )
                }

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // Control independiente del menú.
                Button(
                    onClick = { menuExpanded = !menuExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(
                        text = if (menuExpanded) "<<" else ">"
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // Opciones disponibles para el usuario autenticado.
                menuItems.forEach { item ->

                    Button(
                        onClick = {
                            selectedOption = item.option
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text =
                                if (menuExpanded) {
                                    when (item.option) {
                                        MainMenuOption.WEIGHING ->
                                            "⚖ ${item.label}"

                                        MainMenuOption.HISTORY ->
                                            " ${item.label}"
                                    }

                                } else {
                                    when (item.option) {
                                        MainMenuOption.WEIGHING -> "P"
                                        MainMenuOption.HISTORY -> "H"
                                    }
                                }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // Permite cerrar la sesión desde cualquier pantalla de trabajo.
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (menuExpanded) {
                            "🚪  Cerrar sesión"
                        } else {
                            "🚪"
                        }
                    )
                }
            }
        }

        VerticalDivider()

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {

            // Área principal de trabajo.
            when (selectedOption) {

                MainMenuOption.WEIGHING -> {

                    // La funcionalidad de ¨Pesaje se implementará
                    // en la actividad correspondiente.
                }

                MainMenuOption.HISTORY -> {
                    // La funcionalidad de Historial se implementará
                    // en la actividad correspondiente.
                }

                null -> {
                    // Área principal inicialmente vacía.
                }
            }
        }
    }
}
