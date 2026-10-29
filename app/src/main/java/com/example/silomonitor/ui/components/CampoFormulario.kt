package com.example.silomonitor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.ui.theme.colores

/**
 * Campo de texto reutilizable con retroalimentación visual por campo:
 * - Ícono de error (rojo) y mensaje en supportingText cuando [error] no es null.
 * - Ícono de check (verde) cuando el campo tiene contenido válido.
 * - Contador de caracteres opcional.
 * La validación NO ocurre aquí: el mensaje de error lo calcula el ViewModel.
 */
@Composable
fun CampoFormulario(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    textoAyuda: String? = null,
    iconoInicio: ImageVector? = null,
    sufijo: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int? = null,
    habilitado: Boolean = true,
    mostrarValido: Boolean = true,
    accionFinal: (@Composable () -> Unit)? = null
) {
    val tieneError = error != null
    val esValido = !tieneError && valor.isNotBlank() && mostrarValido
    val colorValido = EstadoSilo.NORMAL.colores().principal

    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->
            if (maxLength == null || nuevo.length <= maxLength) onValorChange(nuevo)
        },
        modifier = modifier.fillMaxWidth(),
        enabled = habilitado,
        label = { Text(etiqueta) },
        leadingIcon = if (iconoInicio != null) {
            { Icon(iconoInicio, contentDescription = null) }
        } else {
            null
        },
        trailingIcon = if (accionFinal != null || tieneError || esValido) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    accionFinal?.invoke()
                    when {
                        tieneError -> Icon(
                            Icons.Filled.Error,
                            contentDescription = "Campo con error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        esValido -> Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Campo válido",
                            tint = colorValido,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            }
        } else {
            null
        },
        suffix = if (sufijo != null) {
            { Text(sufijo) }
        } else {
            null
        },
        supportingText = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = error ?: textoAyuda ?: "",
                    color = if (tieneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (maxLength != null) {
                    Text("${valor.length}/$maxLength")
                }
            }
        },
        isError = tieneError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = minLines
    )
}

/** Mensaje de error para campos que no son de texto (radios, foto, etc.). */
@Composable
fun MensajeErrorCampo(error: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
