package com.example.silomonitor.data.seed

import com.example.silomonitor.data.local.entity.AlertaEntity
import com.example.silomonitor.data.local.entity.GranjaEntity
import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.data.local.entity.SiloEntity
import com.example.silomonitor.data.local.entity.UsuarioEntity
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.util.Seguridad
import kotlin.random.Random

/**
 * Datos ficticios para el MVP (el caso propone 3 granjas con 4 silos cada una = 12 silos).
 * Se generan una sola vez, la primera vez que se abre la app, y quedan guardados en Room.
 */
object DatosSimulados {

    private const val HORA = 3_600_000L
    private const val DIA = 24 * HORA
    private const val DIAS_HISTORIAL = 14

    /** Usuarios pre-registrados por el administrador (no existe registro público). */
    data class UsuarioDemo(val nombre: String, val correo: String, val clave: String, val rol: Rol)

    val usuariosDemo = listOf(
        UsuarioDemo("Juan Pérez", "operario@silomonitor.test", "Operario123", Rol.OPERARIO),
        UsuarioDemo("Carla Muñoz", "supervisor@silomonitor.test", "Super1234", Rol.SUPERVISOR),
        UsuarioDemo("Roberto Díaz", "jefatura@silomonitor.test", "Jefatura123", Rol.JEFATURA)
    )

    private val granjas = listOf(
        GranjaEntity(1, "Granja El Peumo", "Melipilla"),
        GranjaEntity(2, "Granja Santa Elena", "San Pedro"),
        GranjaEntity(3, "Granja Los Aromos", "Alhué")
    )

    /** Configuración base de cada silo: porcentaje actual y consumo diario (% de la capacidad). */
    private data class SiloBase(
        val id: String,
        val granjaId: Int,
        val tipoAlimento: String,
        val capacidadKg: Double,
        val porcentajeActual: Double,
        val consumoDiarioPct: Double
    )

    private val silosBase = listOf(
        SiloBase("P-01", 1, "Inicio pollo broiler", 18_000.0, 72.0, 0.055),
        SiloBase("P-02", 1, "Crecimiento pollo broiler", 22_000.0, 26.0, 0.060),
        SiloBase("P-03", 1, "Terminación pollo broiler", 25_000.0, 54.0, 0.050),
        SiloBase("P-04", 1, "Terminación pollo broiler", 25_000.0, 15.0, 0.065),
        SiloBase("E-01", 2, "Postura gallinas", 15_000.0, 88.0, 0.045),
        SiloBase("E-02", 2, "Postura gallinas", 15_000.0, 41.0, 0.050),
        SiloBase("E-03", 2, "Recría pollitas", 12_000.0, 23.0, 0.055),
        SiloBase("E-04", 2, "Crecimiento pavo", 20_000.0, 63.0, 0.050),
        SiloBase("A-01", 3, "Engorda pavo", 30_000.0, 11.0, 0.060),
        SiloBase("A-02", 3, "Engorda pavo", 30_000.0, 47.0, 0.045),
        SiloBase("A-03", 3, "Inicio pavo", 16_000.0, 35.0, 0.050),
        SiloBase("A-04", 3, "Engorda cerdos", 28_000.0, 81.0, 0.040)
    )

    data class DatosIniciales(
        val granjas: List<GranjaEntity>,
        val silos: List<SiloEntity>,
        val movimientos: List<MovimientoEntity>,
        val alertas: List<AlertaEntity>,
        val usuarios: List<UsuarioEntity>
    )

    fun generar(ahora: Long = System.currentTimeMillis()): DatosIniciales {
        val operario = usuariosDemo[0].nombre
        val supervisor = usuariosDemo[1].nombre
        val silos = mutableListOf<SiloEntity>()
        val movimientos = mutableListOf<MovimientoEntity>()
        val alertas = mutableListOf<AlertaEntity>()

        silosBase.forEachIndexed { indice, base ->
            val random = Random(indice + 7) // semilla fija: siempre los mismos datos
            val nombre = "Silo ${base.id}"
            val granjaNombre = granjas.first { it.id == base.granjaId }.nombre
            val consumoDiario = base.capacidadKg * base.consumoDiarioPct
            val movimientosSilo = mutableListOf<MovimientoEntity>()

            // Se reconstruye el historial hacia atrás a partir del nivel actual
            var nivel = base.capacidadKg * base.porcentajeActual / 100.0
            for (dia in 1..DIAS_HISTORIAL) {
                val fecha = ahora - dia * DIA + random.nextLong(-3 * HORA, 3 * HORA)
                val consumo = consumoDiario * random.nextDouble(0.85, 1.15)
                movimientosSilo += MovimientoEntity(
                    siloId = base.id,
                    tipo = TipoMovimiento.CONSUMO.name,
                    cantidadKg = consumo,
                    nivelResultanteKg = nivel,
                    fecha = fecha,
                    observacion = "Consumo diario de los planteles",
                    usuario = operario
                )
                nivel += consumo

                // Algunas descargas por traslado entre granjas
                if (dia == 6 && random.nextDouble() < 0.35) {
                    val descarga = base.capacidadKg * 0.03
                    movimientosSilo += MovimientoEntity(
                        siloId = base.id,
                        tipo = TipoMovimiento.DESCARGA.name,
                        cantidadKg = descarga,
                        nivelResultanteKg = nivel,
                        fecha = fecha - HORA,
                        observacion = "Traslado de alimento a otra granja",
                        usuario = supervisor
                    )
                    nivel += descarga
                }

                // Si el nivel "antes" queda muy alto, hubo una carga (reposición) ese día
                if (nivel >= base.capacidadKg * 0.86) {
                    nivel = nivel.coerceAtMost(base.capacidadKg)
                    val nivelAntes = base.capacidadKg * random.nextDouble(0.16, 0.30)
                    val fechaCarga = fecha - 2 * HORA
                    movimientosSilo += MovimientoEntity(
                        siloId = base.id,
                        tipo = TipoMovimiento.CARGA.name,
                        cantidadKg = nivel - nivelAntes,
                        nivelResultanteKg = nivel,
                        fecha = fechaCarga,
                        observacion = "Reposición desde planta de alimentos",
                        usuario = supervisor
                    )
                    // Alerta histórica (ya atendida) que originó esa reposición
                    val pctAntes = CalculosSilo.porcentaje(nivelAntes, base.capacidadKg)
                    val estadoAntes = CalculosSilo.estado(pctAntes)
                    if (estadoAntes != EstadoSilo.NORMAL) {
                        alertas += AlertaEntity(
                            siloId = base.id,
                            nivel = estadoAntes.name,
                            porcentaje = pctAntes,
                            mensaje = CalculosSilo.mensajeAlerta(nombre, granjaNombre, estadoAntes, pctAntes),
                            fecha = fechaCarga - 8 * HORA,
                            atendida = true,
                            atendidaPor = supervisor,
                            fechaAtencion = fechaCarga
                        )
                    }
                    nivel = nivelAntes
                }
            }
            movimientos += movimientosSilo

            val nivelActual = base.capacidadKg * base.porcentajeActual / 100.0
            val ultimaActualizacion = ahora - random.nextLong(10, 90) * 60_000
            val consumoCalculado = CalculosSilo.consumoPromedioDiario(movimientosSilo, ahora)
            silos += SiloEntity(
                id = base.id,
                granjaId = base.granjaId,
                nombre = nombre,
                tipoAlimento = base.tipoAlimento,
                capacidadKg = base.capacidadKg,
                nivelKg = nivelActual,
                consumoDiarioKg = if (consumoCalculado > 0) consumoCalculado else consumoDiario,
                ultimaActualizacion = ultimaActualizacion
            )

            // Alerta activa para los silos que hoy están bajo el 30 %
            val estadoActual = CalculosSilo.estado(base.porcentajeActual)
            if (estadoActual != EstadoSilo.NORMAL) {
                alertas += AlertaEntity(
                    siloId = base.id,
                    nivel = estadoActual.name,
                    porcentaje = base.porcentajeActual,
                    mensaje = CalculosSilo.mensajeAlerta(nombre, granjaNombre, estadoActual, base.porcentajeActual),
                    fecha = ultimaActualizacion - random.nextLong(1, 6) * HORA
                )
            }
        }

        val usuarios = usuariosDemo.map {
            UsuarioEntity(
                correo = it.correo,
                nombre = it.nombre,
                rol = it.rol.name,
                claveHash = Seguridad.sha256(it.clave)
            )
        }

        return DatosIniciales(granjas, silos, movimientos, alertas, usuarios)
    }
}
