package com.example.silomonitor

import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas unitarias de las reglas de negocio (se ejecutan sin emulador). */
class CalculosSiloTest {

    @Test
    fun `estado normal sobre 30 por ciento`() {
        assertEquals(EstadoSilo.NORMAL, CalculosSilo.estado(30.1))
        assertEquals(EstadoSilo.NORMAL, CalculosSilo.estado(85.0))
    }

    @Test
    fun `estado advertencia entre 20 y 30 por ciento`() {
        assertEquals(EstadoSilo.ADVERTENCIA, CalculosSilo.estado(30.0))
        assertEquals(EstadoSilo.ADVERTENCIA, CalculosSilo.estado(25.0))
        assertEquals(EstadoSilo.ADVERTENCIA, CalculosSilo.estado(20.0))
    }

    @Test
    fun `estado critico bajo 20 por ciento`() {
        assertEquals(EstadoSilo.CRITICO, CalculosSilo.estado(19.9))
        assertEquals(EstadoSilo.CRITICO, CalculosSilo.estado(0.0))
    }

    @Test
    fun `porcentaje se calcula y se limita entre 0 y 100`() {
        assertEquals(50.0, CalculosSilo.porcentaje(5_000.0, 10_000.0), 0.001)
        assertEquals(100.0, CalculosSilo.porcentaje(12_000.0, 10_000.0), 0.001)
        assertEquals(0.0, CalculosSilo.porcentaje(100.0, 0.0), 0.001)
    }

    @Test
    fun `carga suma y descarga o consumo restan`() {
        assertEquals(7_000.0, CalculosSilo.nivelResultante(5_000.0, TipoMovimiento.CARGA, 2_000.0), 0.001)
        assertEquals(3_000.0, CalculosSilo.nivelResultante(5_000.0, TipoMovimiento.DESCARGA, 2_000.0), 0.001)
        assertEquals(4_000.0, CalculosSilo.nivelResultante(5_000.0, TipoMovimiento.CONSUMO, 1_000.0), 0.001)
    }

    @Test
    fun `dias restantes segun consumo diario`() {
        assertEquals(5, CalculosSilo.diasRestantes(5_000.0, 1_000.0))
        assertEquals(2, CalculosSilo.diasRestantes(2_500.0, 1_000.0))
        assertNull(CalculosSilo.diasRestantes(5_000.0, 0.0))
    }

    @Test
    fun `consumo promedio solo considera salidas de los ultimos 7 dias`() {
        val ahora = 30L * 86_400_000L
        val dia = 86_400_000L
        val movimientos = listOf(
            movimiento(TipoMovimiento.CONSUMO, 700.0, ahora - 1 * dia),
            movimiento(TipoMovimiento.DESCARGA, 700.0, ahora - 2 * dia),
            movimiento(TipoMovimiento.CARGA, 10_000.0, ahora - 3 * dia), // no cuenta: es una entrada
            movimiento(TipoMovimiento.CONSUMO, 5_000.0, ahora - 10 * dia) // no cuenta: fuera de rango
        )
        assertEquals(200.0, CalculosSilo.consumoPromedioDiario(movimientos, ahora), 0.001)
    }

    private fun movimiento(tipo: TipoMovimiento, cantidad: Double, fecha: Long) = MovimientoEntity(
        siloId = "P-01",
        tipo = tipo.name,
        cantidadKg = cantidad,
        nivelResultanteKg = 0.0,
        fecha = fecha,
        observacion = "",
        usuario = "Prueba"
    )
}
