package com.example.silomonitor.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manejo de archivos de imagen en el almacenamiento privado de la app.
 * - Cámara: se crea un archivo vacío y se entrega su Uri (FileProvider) a la app de cámara.
 * - Galería: la imagen elegida se copia a la carpeta privada, así la Uri sigue siendo válida
 *   después de reiniciar la app (las Uri de la galería son permisos temporales).
 */
object ImagenHelper {

    private const val CARPETA = "imagenes"

    private fun autoridad(context: Context) = "${context.packageName}.fileprovider"

    private fun nuevoArchivo(context: Context, prefijo: String): File {
        val carpeta = File(context.filesDir, CARPETA).apply { mkdirs() }
        return File(carpeta, "${prefijo}_${System.currentTimeMillis()}.jpg")
    }

    /** Uri donde la cámara guardará la foto (Guía 13: TakePicture + FileProvider). */
    fun crearUriParaFoto(context: Context, prefijo: String): Uri? = runCatching {
        FileProvider.getUriForFile(context, autoridad(context), nuevoArchivo(context, prefijo))
    }.getOrNull()

    /** Copia la imagen elegida en la galería a la carpeta privada de la app. */
    suspend fun copiarDesdeGaleria(context: Context, origen: Uri, prefijo: String): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                val destino = nuevoArchivo(context, prefijo)
                val entrada = context.contentResolver.openInputStream(origen)
                    ?: error("No se pudo leer la imagen seleccionada")
                entrada.use { input ->
                    destino.outputStream().use { output -> input.copyTo(output) }
                }
                FileProvider.getUriForFile(context, autoridad(context), destino)
            }.getOrNull()
        }

    /** Elimina un archivo creado por la app (por ejemplo, si se cancela la cámara). */
    fun eliminar(context: Context, uri: Uri?) {
        if (uri == null) return
        runCatching { context.contentResolver.delete(uri, null, null) }
    }
}
