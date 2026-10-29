package com.example.silomonitor.ui.screens.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.R
import com.example.silomonitor.data.seed.DatosSimulados
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.ui.components.CampoFormulario
import com.example.silomonitor.ui.components.MensajeError
import com.example.silomonitor.ui.components.rememberSacudida
import com.example.silomonitor.viewmodel.LoginViewModel

/** Inicio de sesión con usuarios pre-registrados (operario, supervisor o jefatura). */
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val focusManager = LocalFocusManager.current
    val sacudida = rememberSacudida(estado.intentosFallidos)

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            if (evento is EventoUi.NavegarInicio) onLoginExitoso()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_silo),
                contentDescription = "Logo SiloMonitor",
                modifier = Modifier.size(110.dp)
            )
            Text("SiloMonitor", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Ingresa con tu cuenta o crea una nueva",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Formulario (se sacude si hay errores)
            ElevatedCard(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .graphicsLayer { translationX = sacudida.value }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))

                    CampoFormulario(
                        valor = estado.correo,
                        onValorChange = viewModel::onCorreoChange,
                        etiqueta = "Correo electrónico",
                        iconoInicio = Icons.Filled.Email,
                        error = estado.errores.correo,
                        habilitado = !estado.cargando,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    CampoFormulario(
                        valor = estado.clave,
                        onValorChange = viewModel::onClaveChange,
                        etiqueta = "Contraseña",
                        iconoInicio = Icons.Filled.Lock,
                        error = estado.errores.clave,
                        habilitado = !estado.cargando,
                        mostrarValido = false,
                        visualTransformation = if (estado.mostrarClave) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.iniciarSesion()
                        }),
                        accionFinal = {
                            IconButton(onClick = viewModel::alternarMostrarClave) {
                                Icon(
                                    imageVector = if (estado.mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (estado.mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = estado.recordar,
                            onCheckedChange = viewModel::onRecordarChange,
                            enabled = !estado.cargando
                        )
                        Text("Mantener la sesión iniciada", style = MaterialTheme.typography.bodyMedium)
                    }

                    MensajeError(texto = estado.errorGeneral)
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.iniciarSesion()
                        },
                        enabled = !estado.cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        // Transición animada entre el texto y el indicador de carga
                        AnimatedContent(
                            targetState = estado.cargando,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "boton_login"
                        ) { cargando ->
                            if (cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Ingresar")
                            }
                        }
                    }
                }
            }

            // Acceso al registro para quienes aún no tienen cuenta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onIrARegistro, enabled = !estado.cargando) {
                    Text("Regístrate")
                }
            }

            UsuariosDemo(
                usuarios = viewModel.usuariosDemo,
                onSeleccionar = viewModel::usarUsuarioDemo,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/** Accesos rápidos a los usuarios ficticios para la demostración del MVP. */
@Composable
private fun UsuariosDemo(
    usuarios: List<DatosSimulados.UsuarioDemo>,
    onSeleccionar: (DatosSimulados.UsuarioDemo) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                Text("Usuarios de prueba (toca uno para completar)", style = MaterialTheme.typography.labelLarge)
            }
            usuarios.forEachIndexed { indice, usuario ->
                if (indice > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("${usuario.rol.etiqueta} · ${usuario.nombre}") },
                    supportingContent = { Text("${usuario.correo} / ${usuario.clave}") },
                    leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onSeleccionar(usuario) }
                )
            }
        }
    }
}
