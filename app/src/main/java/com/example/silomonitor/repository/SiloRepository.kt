package com.example.silomonitor.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.silomonitor.data.local.SiloDatabase
import com.example.silomonitor.data.local.entity.AlertaDetalle
import com.example.silomonitor.data.local.entity.AlertaEntity
import com.example.silomonitor.data.local.entity.GranjaEntity
import com.example.silomonitor.data.local.entity.IncidenciaEntity
import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.data.local.entity.SiloConGranja
import com.example.silomonitor.data.local.entity.SiloEntity
import com.example.silomonitor.data.preferences.PreferenciasDataStore
import com.example.silomonitor.data.remote.SiloApiSimulada
import com.example.silomonitor.data.seed.DatosSimulados
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.Prioridad
import com.example.silomonitor.model.ResultadoMovimiento
import com.example.silomonitor.model.ResultadoSincronizacion
import com.example.silomonitor.model.SiloItem
import com.example.silomonitor.model.TipoIncidencia
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.util.NotificacionHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Única fuente de datos de silos para los ViewModels (patrón Repository).
 * Combina la base local (Room), la API simulada y las reglas de alertas.
 * Las pantallas nunca acceden directamente a Room ni a la API.
 */
class SiloRepository(
    private val context: Context,
    private val db: SiloDatabase,
    private val api: SiloApiSimulada,
    private val preferencias: PreferenciasDataStore
) {
    private val granjaDao = db.granjaDao()
    private val siloDao = db.siloDao()
    private val movimientoDao = db.movimientoDao()
    private val alertaDao = db.alertaDao()
    private val incidenciaDao = db.incidenciaDao()
    private val usuarioDao = db.usuarioDao()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    // ---------------------------- Consultas reactivas ----------------------------

    val silos: Flow<List<SiloItem>> =
        siloDao.observarSilosConGranja().map { lista -> lista.map { it.aSiloItem() } }

    val granjas: Flow<List<GranjaEntity>> = granjaDao.observarTodas()

    val alertas: Flow<List<AlertaDetalle>> = alertaDao.observarDetalle()

    val alertasActivas: Flow<Int> = alertaDao.observarCantidadActivas()

    val ultimaSincronizacion: Flow<Long?> = preferencias.ultimaSincronizacion

    fun silo(id: String): Flow<SiloItem?> = siloDao.observarSiloConGranja(id).map { it?.aSiloItem() }

    fun movimientos(siloId: String): Flow<List<MovimientoEntity>> = movimientoDao.observarPorSilo(siloId)

    fun incidencias(siloId: String): Flow<List<IncidenciaEntity>> = incidenciaDao.observarPorSilo(siloId)

    // ---------------------------- Datos iniciales ----------------------------

    /** Carga los datos simulados la primera vez que se abre la app. */
    suspend fun inicializarSiEsNecesario() {
        if (siloDao.contar() > 0 && usuarioDao.contar() > 0) return
        val datos = DatosSimulados.generar()
        db.withTransaction {
            granjaDao.insertarTodas(datos.granjas)
            siloDao.insertarTodos(datos.silos)
            movimientoDao.insertarTodos(datos.movimientos)
            alertaDao.insertarTodas(datos.alertas)
            usuarioDao.insertarTodos(datos.usuarios)
        }
    }

    /** Opción de demostración: borra todo y vuelve a cargar los datos simulados. */
    suspend fun restablecerDatos() {
        withContext(Dispatchers.IO) { db.clearAllTables() }
        inicializarSiEsNecesario()
    }

    // ---------------------------- Movimientos ----------------------------

    suspend fun registrarMovimiento(
        siloId: String,
        tipo: TipoMovimiento,
        cantidadKg: Double,
        observacion: String,
        usuario: String
    ): Result<ResultadoMovimiento> = runCatching {
        val (resultado, alertaNueva) = db.withTransaction {
            val silo = siloDao.obtener(siloId) ?: throw IllegalArgumentException("Silo no encontrado")
            val nuevoNivel = CalculosSilo.nivelResultante(silo.nivelKg, tipo, cantidadKg)
            // Validación defensiva: la UI ya validó, pero la regla se vuelve a asegurar aquí
            require(cantidadKg > 0 && nuevoNivel >= 0.0 && nuevoNivel <= silo.capacidadKg) {
                "El movimiento deja el silo fuera de su capacidad"
            }
            val ahora = System.currentTimeMillis()
            movimientoDao.insertar(
                MovimientoEntity(
                    siloId = siloId,
                    tipo = tipo.name,
                    cantidadKg = cantidadKg,
                    nivelResultanteKg = nuevoNivel,
                    fecha = ahora,
                    observacion = observacion,
                    usuario = usuario
                )
            )
            val actualizado = silo.copy(
                nivelKg = nuevoNivel,
                consumoDiarioKg = recalcularConsumo(silo, ahora),
                ultimaActualizacion = ahora
            )
            siloDao.actualizar(actualizado)
            val alerta = evaluarAlertas(silo, actualizado, usuario)
            val pctNuevo = CalculosSilo.porcentaje(actualizado.nivelKg, actualizado.capacidadKg)
            ResultadoMovimiento(
                estadoAnterior = CalculosSilo.estado(CalculosSilo.porcentaje(silo.nivelKg, silo.capacidadKg)),
                estadoNuevo = CalculosSilo.estado(pctNuevo),
                porcentajeNuevo = pctNuevo,
                alertaGenerada = alerta != null
            ) to alerta
        }
        alertaNueva?.let { notificar(it, siloNombre = siloId) }
        resultado
    }

    // ---------------------------- Incidencias ----------------------------

    suspend fun reportarIncidencia(
        siloId: String,
        tipo: TipoIncidencia,
        prioridad: Prioridad,
        descripcion: String,
        fotoUri: String?,
        usuario: String
    ): Result<Long> = runCatching {
        incidenciaDao.insertar(
            IncidenciaEntity(
                siloId = siloId,
                tipo = tipo.name,
                prioridad = prioridad.name,
                descripcion = descripcion,
                fotoUri = fotoUri,
                fecha = System.currentTimeMillis(),
                usuario = usuario
            )
        )
    }

    // ---------------------------- Alertas ----------------------------

    suspend fun atenderAlerta(id: Long, usuario: String) {
        alertaDao.atender(id, usuario, System.currentTimeMillis())
    }

    // ---------------------------- Sincronización (API) ----------------------------

    /**
     * Consulta la API simulada y guarda las lecturas en Room.
     * Si falla (sin conexión), la app sigue mostrando los últimos datos guardados localmente.
     */
    suspend fun sincronizar(): Result<ResultadoSincronizacion> {
        if (_sincronizando.value) {
            return Result.failure(IllegalStateException("Ya hay una sincronización en curso"))
        }
        _sincronizando.value = true
        return try {
            val silosActuales = siloDao.obtenerTodos()
            val lecturas = api.obtenerLecturas(silosActuales, preferencias.simularSinConexion.first())
            val alertasNuevas = db.withTransaction {
                val generadas = mutableListOf<AlertaEntity>()
                lecturas.forEach { lectura ->
                    val silo = silosActuales.firstOrNull { it.id == lectura.siloId } ?: return@forEach
                    val consumo = silo.nivelKg - lectura.nivelKg
                    if (consumo > 0.0) {
                        movimientoDao.insertar(
                            MovimientoEntity(
                                siloId = silo.id,
                                tipo = TipoMovimiento.CONSUMO.name,
                                cantidadKg = consumo,
                                nivelResultanteKg = lectura.nivelKg,
                                fecha = lectura.fechaLectura,
                                observacion = "Lectura automática del sensor",
                                usuario = "Sistema de monitoreo"
                            )
                        )
                    }
                    val actualizado = silo.copy(
                        nivelKg = lectura.nivelKg,
                        consumoDiarioKg = recalcularConsumo(silo, lectura.fechaLectura),
                        ultimaActualizacion = lectura.fechaLectura
                    )
                    siloDao.actualizar(actualizado)
                    evaluarAlertas(silo, actualizado, "Sistema de monitoreo")?.let { generadas += it }
                }
                generadas
            }
            preferencias.guardarUltimaSincronizacion(System.currentTimeMillis())
            alertasNuevas.forEach { notificar(it, siloNombre = it.siloId) }
            Result.success(ResultadoSincronizacion(lecturas.size, alertasNuevas.size))
        } catch (e: CancellationException) {
            throw e // nunca se ocultan cancelaciones de corrutinas
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _sincronizando.value = false
        }
    }

    // ---------------------------- Reglas internas ----------------------------

    private suspend fun recalcularConsumo(silo: SiloEntity, ahora: Long): Double {
        val desde = ahora - CalculosSilo.DIAS_PROMEDIO_CONSUMO * 86_400_000L
        val recientes = movimientoDao.obtenerDesde(silo.id, desde)
        val promedio = CalculosSilo.consumoPromedioDiario(recientes, ahora)
        return if (promedio > 0.0) promedio else silo.consumoDiarioKg
    }

    /**
     * Regla de alertas:
     * - Si el silo empeora (Normal → Advertencia → Crítico) se crea una alerta.
     * - Si vuelve a Normal (por una carga), sus alertas activas se cierran automáticamente.
     */
    private suspend fun evaluarAlertas(antes: SiloEntity, despues: SiloEntity, usuario: String): AlertaEntity? {
        val estadoAntes = CalculosSilo.estado(CalculosSilo.porcentaje(antes.nivelKg, antes.capacidadKg))
        val pctDespues = CalculosSilo.porcentaje(despues.nivelKg, despues.capacidadKg)
        val estadoDespues = CalculosSilo.estado(pctDespues)
        val ahora = System.currentTimeMillis()

        if (estadoDespues == EstadoSilo.NORMAL && estadoAntes != EstadoSilo.NORMAL) {
            alertaDao.atenderPorSilo(despues.id, "Reposición registrada por $usuario", ahora)
        }
        if (estadoDespues.gravedad > estadoAntes.gravedad) {
            val granja = granjaDao.obtener(despues.granjaId)?.nombre.orEmpty()
            val alerta = AlertaEntity(
                siloId = despues.id,
                nivel = estadoDespues.name,
                porcentaje = pctDespues,
                mensaje = CalculosSilo.mensajeAlerta(despues.nombre, granja, estadoDespues, pctDespues),
                fecha = ahora
            )
            val id = alertaDao.insertar(alerta)
            return alerta.copy(id = id)
        }
        return null
    }

    private suspend fun notificar(alerta: AlertaEntity, siloNombre: String) {
        if (!preferencias.notificacionesActivas.first()) return
        val estado = EstadoSilo.desde(alerta.nivel)
        NotificacionHelper.mostrarAlerta(
            context = context,
            id = alerta.id.toInt(),
            titulo = "Silo $siloNombre · nivel ${estado.etiqueta.lowercase()}",
            mensaje = alerta.mensaje
        )
    }

    private fun SiloConGranja.aSiloItem(): SiloItem {
        val pct = CalculosSilo.porcentaje(silo.nivelKg, silo.capacidadKg)
        return SiloItem(
            id = silo.id,
            nombre = silo.nombre,
            granjaId = silo.granjaId,
            granjaNombre = granjaNombre,
            tipoAlimento = silo.tipoAlimento,
            capacidadKg = silo.capacidadKg,
            nivelKg = silo.nivelKg,
            consumoDiarioKg = silo.consumoDiarioKg,
            ultimaActualizacion = silo.ultimaActualizacion,
            porcentaje = pct,
            estado = CalculosSilo.estado(pct),
            diasRestantes = CalculosSilo.diasRestantes(silo.nivelKg, silo.consumoDiarioKg)
        )
    }
}
