package com.example.silomonitor.ui.screens.registro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.Rol
import com.example.silomonitor.ui.components.CampoFormulario
import com.example.silomonitor.ui.components.MensajeError
import com.example.silomonitor.ui.components.MensajeErrorCampo
import com.example.silomonitor.ui.components.rememberSacudida
import com.example.silomonitor.viewmodel.RegistroViewModel

/** Creación de una cuenta nueva eligiendo el cargo: operario, supervisor o jefatura. */
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onVolverAlLogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val focusManager = LocalFocusManager.current
    val sacudida = rememberSacudida(estado.intentosFallidos)

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            if (evento is EventoUi.NavegarInicio) onRegistroExitoso()
        }
    }

    val transformacionClave = if (estado.mostrarClave) VisualTransformation.None else PasswordVisualTransformation()

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolverAlLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al inicio de sesión")
                }
                Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
            }

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
                    Text("Tus datos", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))

                    CampoFormulario(
                        valor = estado.nombre,
                        onValorChange = viewModel::onNombreChange,
                        etiqueta = "Nombre y apellido *",
                        iconoInicio = Icons.Filled.Person,
                        error = estado.errores.nombre,
                        habilitado = !estado.cargando,
                        maxLength = Validadores.NOMBRE_MAX,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    CampoFormulario(
                        valor = estado.correo,
                        onValorChange = viewModel::onCorreoChange,
                        etiqueta = "Correo electrónico *",
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
                        etiqueta = "Contraseña *",
                        iconoInicio = Icons.Filled.Lock,
                        error = estado.errores.clave,
                        textoAyuda = "Mínimo ${Validadores.CLAVE_NUEVA_MIN} caracteres, con letras y números",
                        habilitado = !estado.cargando,
                        visualTransformation = transformacionClave,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        accionFinal = {
                            IconButton(onClick = viewModel::alternarMostrarClave) {
                                Icon(
                                    imageVector = if (estado.mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (estado.mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )
                    CampoFormulario(
                        valor = estado.confirmarClave,
                        onValorChange = viewModel::onConfirmarClaveChange,
                        etiqueta = "Repetir contraseña *",
                        iconoInicio = Icons.Filled.Lock,
                        error = estado.errores.confirmarClave,
                        habilitado = !estado.cargando,
                        visualTransformation = transformacionClave,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("¿En qué te desempeñarás? *", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tu cargo define qué puedes hacer dentro de la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    SelectorRol(
                        seleccionado = estado.rol,
                        habilitado = !estado.cargando,
                        onSeleccionar = viewModel::onRolChange
                    )
                    MensajeErrorCampo(estado.errores.rol)

                    MensajeError(texto = estado.errorGeneral)
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.registrar()
                        },
                        enabled = !estado.cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        AnimatedContent(
                            targetState = estado.cargando,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "boton_registro"
                        ) { cargando ->
                            if (cargando) {
                                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Crear cuenta")
                            }
                        }
                    }
                    Text(
                        "* Campos obligatorios",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(onClick = onVolverAlLogin, enabled = !estado.cargando) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

/** Tarjetas seleccionables (comportamiento de radio button) para elegir el cargo. */
@Composable
private fun SelectorRol(
    seleccionado: Rol?,
    habilitado: Boolean,
    onSeleccionar: (Rol) -> Unit
) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Rol.entries.forEach { rol ->
            val elegido = seleccionado == rol
            val colorBorde by animateColorAsState(
                targetValue = if (elegido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                animationSpec = tween(300),
                label = "borde_rol"
            )
            val colorFondo by animateColorAsState(
                targetValue = if (elegido) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                animationSpec = tween(300),
                label = "fondo_rol"
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorFondo,
                border = BorderStroke(if (elegido) 2.dp else 1.dp, colorBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = elegido,
                        enabled = habilitado,
                        role = Role.RadioButton,
                        onClick = { onSeleccionar(rol) }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = iconoRol(rol),
                        contentDescription = null,
                        tint = if (elegido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(rol.etiqueta, style = MaterialTheme.typography.titleSmall)
                        Text(
                            rol.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RadioButton(selected = elegido, onClick = null, enabled = habilitado)
                }
            }
        }
    }
}

private fun iconoRol(rol: Rol): ImageVector = when (rol) {
    Rol.OPERARIO -> Icons.Filled.Build
    Rol.SUPERVISOR -> Icons.Filled.SupervisorAccount
    Rol.JEFATURA -> Icons.Filled.Assessment
}
