package com.example.silomonitor.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Botones "Cámara" y "Galería" con el API moderno de ActivityResult (Guía 13):
 * - Cámara: verifica/solicita el permiso CAMERA y lanza TakePicture() con una Uri de FileProvider.
 * - Galería: lanza GetContent() filtrando solo imágenes.
 * Si el usuario cancela o niega el permiso, la app no se cae: se informa con un mensaje.
 */
@Composable
fun SelectorImagen(
    onCrearUriCamara: () -> Uri?,
    onResultadoCamara: (Boolean) -> Unit,
    onImagenGaleria: (Uri) -> Unit,
    onMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val context = LocalContext.current

    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        onResultadoCamara(exito)
        if (!exito) onMensaje("Captura cancelada")
    }

    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImagenGaleria(uri)
    }

    fun abrirCamara() {
        val uri = onCrearUriCamara()
        if (uri == null) {
            onMensaje("No se pudo preparar el archivo para la foto")
            return
        }
        try {
            camaraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            onResultadoCamara(false)
            onMensaje("No se encontró una aplicación de cámara en el dispositivo")
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else onMensaje("Permiso de cámara denegado. Puedes activarlo en Ajustes > Apps.")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = {
                val tienePermiso = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (tienePermiso) abrirCamara() else permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            },
            enabled = habilitado,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cámara")
        }
        OutlinedButton(
            onClick = {
                try {
                    galeriaLauncher.launch("image/*")
                } catch (e: ActivityNotFoundException) {
                    onMensaje("No se encontró una galería de imágenes")
                }
            },
            enabled = habilitado,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Galería")
        }
    }
}
