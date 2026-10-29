package com.example.silomonitor.domain.validation

import com.example.silomonitor.model.Prioridad
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.TipoIncidencia
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.util.Formatos

/**
 * Validaciones centralizadas y desacopladas de la interfaz.
 * Cada función devuelve el mensaje de error del campo, o null si el valor es válido.
 * Los ViewModels las usan y la UI solo muestra el resultado (isError + supportingText).
 */
object Validadores {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_ENTERO = Regex("^\\d+$")

    const val CLAVE_MIN = 6
    const val CANTIDAD_MAX_DIGITOS = 7
    const val OBSERVACION_MAX = 200
    const val DESCRIPCION_MIN = 10
    const val DESCRIPCION_MAX = 300

    // ---------------------------- Login ----------------------------

    fun validarCorreo(correo: String): String? {
        val valor = correo.trim()
        return when {
            valor.isEmpty() -> "Ingresa tu correo electrónico"
            !REGEX_CORREO.matches(valor) -> "Formato inválido. Ejemplo: nombre@empresa.cl"
            else -> null
        }
    }

    fun validarClave(clave: String): String? = when {
        clave.isEmpty() -> "Ingresa tu contraseña"
        clave.length < CLAVE_MIN -> "Debe tener al menos $CLAVE_MIN caracteres"
        else -> null
    }

    // ---------------------------- Registro ----------------------------

    private val REGEX_NOMBRE = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' ]+$")
    const val NOMBRE_MIN = 3
    const val NOMBRE_MAX = 50
    const val CLAVE_NUEVA_MIN = 8

    fun validarNombre(nombre: String): String? {
        val valor = nombre.trim()
        return when {
            valor.isEmpty() -> "Ingresa tu nombre completo"
            valor.length < NOMBRE_MIN -> "Debe tener al menos $NOMBRE_MIN letras"
            valor.length > NOMBRE_MAX -> "Máximo $NOMBRE_MAX caracteres"
            !REGEX_NOMBRE.matches(valor) -> "Usa solo letras y espacios"
            !valor.contains(' ') -> "Ingresa nombre y apellido"
            else -> null
        }
    }

    /** Contraseña de una cuenta nueva: más exigente que la del login. */
    fun validarClaveNueva(clave: String): String? = when {
        clave.isEmpty() -> "Crea una contraseña"
        clave.length < CLAVE_NUEVA_MIN -> "Debe tener al menos $CLAVE_NUEVA_MIN caracteres"
        clave.none { it.isLetter() } -> "Debe incluir al menos una letra"
        clave.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun validarConfirmacionClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite tu contraseña"
        confirmacion != clave -> "Las contraseñas no coinciden"
        else -> null
    }

    fun validarRol(rol: Rol?): String? =
        if (rol == null) "Selecciona en qué te desempeñarás" else null

    // ---------------------------- Movimientos ----------------------------

    /** La cantidad se ingresa en kilos enteros y debe respetar la capacidad y el stock del silo. */
    fun validarCantidad(
        texto: String,
        tipo: TipoMovimiento,
        nivelActualKg: Double,
        capacidadKg: Double
    ): String? {
        val valorTexto = texto.trim()
        val capacidadLibre = (capacidadKg - nivelActualKg).coerceAtLeast(0.0)
        // Solo se evalúa como número cuando el texto pasó la validación de formato
        val valor: Double = valorTexto.toLongOrNull()?.toDouble() ?: 0.0
        return when {
            valorTexto.isEmpty() -> "Ingresa la cantidad en kilos"
            !REGEX_ENTERO.matches(valorTexto) -> "Ingresa solo números enteros (sin puntos ni comas)"
            valorTexto.length > CANTIDAD_MAX_DIGITOS -> "La cantidad es demasiado grande"
            valor <= 0.0 -> "La cantidad debe ser mayor a 0"
            tipo == TipoMovimiento.CARGA && valor > capacidadLibre ->
                "Supera la capacidad libre del silo (máx. ${Formatos.kg(capacidadLibre)})"
            tipo != TipoMovimiento.CARGA && valor > nivelActualKg ->
                "Supera el alimento disponible (máx. ${Formatos.kg(nivelActualKg)})"
            else -> null
        }
    }

    /** Regla de negocio: toda descarga debe indicar su motivo. */
    fun validarObservacion(texto: String, obligatoria: Boolean): String? = when {
        obligatoria && texto.isBlank() -> "Indica el motivo de la descarga"
        texto.length > OBSERVACION_MAX -> "Máximo $OBSERVACION_MAX caracteres"
        else -> null
    }

    // ---------------------------- Incidencias ----------------------------

    fun validarTipoIncidencia(tipo: TipoIncidencia?): String? =
        if (tipo == null) "Selecciona el tipo de incidencia" else null

    fun validarDescripcion(texto: String): String? {
        val valor = texto.trim()
        return when {
            valor.isEmpty() -> "Describe la incidencia"
            valor.length < DESCRIPCION_MIN -> "Describe con al menos $DESCRIPCION_MIN caracteres"
            valor.length > DESCRIPCION_MAX -> "Máximo $DESCRIPCION_MAX caracteres"
            else -> null
        }
    }

    /** Regla de negocio: las incidencias de prioridad alta requieren foto. */
    fun validarFotoIncidencia(prioridad: Prioridad, fotoUri: String?): String? =
        if (prioridad == Prioridad.ALTA && fotoUri.isNullOrBlank()) {
            "Las incidencias de prioridad alta requieren una foto como evidencia"
        } else {
            null
        }
}
