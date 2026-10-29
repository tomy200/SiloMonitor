package com.example.silomonitor

import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.Prioridad
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.TipoIncidencia
import com.example.silomonitor.model.TipoMovimiento
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de las validaciones de los formularios (lógica desacoplada de la UI). */
class ValidadoresTest {

    // ---------- Login ----------

    @Test
    fun `correo vacio o mal formado es invalido`() {
        assertNotNull(Validadores.validarCorreo(""))
        assertNotNull(Validadores.validarCorreo("operario"))
        assertNotNull(Validadores.validarCorreo("operario@empresa"))
    }

    @Test
    fun `correo con formato correcto es valido`() {
        assertNull(Validadores.validarCorreo("operario@silomonitor.test"))
        assertNull(Validadores.validarCorreo("  supervisor@empresa.cl  "))
    }

    @Test
    fun `clave debe tener al menos 6 caracteres`() {
        assertNotNull(Validadores.validarClave(""))
        assertNotNull(Validadores.validarClave("12345"))
        assertNull(Validadores.validarClave("123456"))
    }

    // ---------- Registro ----------

    @Test
    fun `nombre debe tener nombre y apellido con letras`() {
        assertNotNull(Validadores.validarNombre(""))
        assertNotNull(Validadores.validarNombre("Tomás"))
        assertNotNull(Validadores.validarNombre("Tomás 123"))
        assertNull(Validadores.validarNombre("Tomás González"))
    }

    @Test
    fun `clave nueva exige 8 caracteres con letras y numeros`() {
        assertNotNull(Validadores.validarClaveNueva("abc123"))
        assertNotNull(Validadores.validarClaveNueva("abcdefgh"))
        assertNotNull(Validadores.validarClaveNueva("12345678"))
        assertNull(Validadores.validarClaveNueva("silos2026"))
    }

    @Test
    fun `confirmacion de clave debe coincidir`() {
        assertNotNull(Validadores.validarConfirmacionClave("silos2026", ""))
        assertNotNull(Validadores.validarConfirmacionClave("silos2026", "silos2025"))
        assertNull(Validadores.validarConfirmacionClave("silos2026", "silos2026"))
    }

    @Test
    fun `registro exige elegir un cargo`() {
        assertNotNull(Validadores.validarRol(null))
        assertNull(Validadores.validarRol(Rol.SUPERVISOR))
    }

    // ---------- Movimientos ----------

    @Test
    fun `cantidad debe ser un entero positivo`() {
        val nivel = 5_000.0
        val capacidad = 10_000.0
        assertNotNull(Validadores.validarCantidad("", TipoMovimiento.CARGA, nivel, capacidad))
        assertNotNull(Validadores.validarCantidad("abc", TipoMovimiento.CARGA, nivel, capacidad))
        assertNotNull(Validadores.validarCantidad("1.500", TipoMovimiento.CARGA, nivel, capacidad))
        assertNotNull(Validadores.validarCantidad("0", TipoMovimiento.CARGA, nivel, capacidad))
        assertNull(Validadores.validarCantidad("1500", TipoMovimiento.CARGA, nivel, capacidad))
    }

    @Test
    fun `carga no puede superar la capacidad libre`() {
        assertNotNull(Validadores.validarCantidad("5001", TipoMovimiento.CARGA, 5_000.0, 10_000.0))
        assertNull(Validadores.validarCantidad("5000", TipoMovimiento.CARGA, 5_000.0, 10_000.0))
    }

    @Test
    fun `descarga y consumo no pueden superar el alimento disponible`() {
        assertNotNull(Validadores.validarCantidad("5001", TipoMovimiento.DESCARGA, 5_000.0, 10_000.0))
        assertNotNull(Validadores.validarCantidad("5001", TipoMovimiento.CONSUMO, 5_000.0, 10_000.0))
        assertNull(Validadores.validarCantidad("5000", TipoMovimiento.CONSUMO, 5_000.0, 10_000.0))
    }

    @Test
    fun `descarga exige motivo`() {
        assertNotNull(Validadores.validarObservacion("", obligatoria = true))
        assertNull(Validadores.validarObservacion("Traslado a otra granja", obligatoria = true))
        assertNull(Validadores.validarObservacion("", obligatoria = false))
    }

    // ---------- Incidencias ----------

    @Test
    fun `incidencia requiere tipo y descripcion suficiente`() {
        assertNotNull(Validadores.validarTipoIncidencia(null))
        assertNull(Validadores.validarTipoIncidencia(TipoIncidencia.FUGA))
        assertNotNull(Validadores.validarDescripcion("corta"))
        assertNull(Validadores.validarDescripcion("Fuga en la compuerta inferior del silo"))
    }

    @Test
    fun `prioridad alta exige foto`() {
        assertNotNull(Validadores.validarFotoIncidencia(Prioridad.ALTA, null))
        assertNull(Validadores.validarFotoIncidencia(Prioridad.ALTA, "content://foto"))
        assertNull(Validadores.validarFotoIncidencia(Prioridad.MEDIA, null))
    }
}
