package com.example.silomonitor.navigation

/** Plantillas de rutas con argumentos (se registran así en el NavHost). */
object Rutas {
    const val ARG_SILO_ID = "siloId"
    const val ARG_TIPO = "tipo"
    const val DETALLE_SILO = "detalle_silo/{siloId}"
    const val REGISTRAR_MOVIMIENTO = "registrar_movimiento/{siloId}?tipo={tipo}"
    const val REPORTAR_INCIDENCIA = "reportar_incidencia/{siloId}"
}

/**
 * Sealed class para definir rutas type-safe en la navegación (Guía 10).
 * - Rutas simples: `data object`.
 * - Rutas con argumentos: `data class` que construye la ruta final con [rutaNavegable].
 */
sealed class Screen(val route: String) {

    /** Ruta concreta a la que se navega (reemplaza los argumentos por sus valores). */
    open fun rutaNavegable(): String = route

    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Registro : Screen("registro")

    // Pestañas principales
    data object Resumen : Screen("resumen")
    data object Silos : Screen("silos")
    data object Alertas : Screen("alertas")
    data object Perfil : Screen("perfil")

    // Pantallas con argumentos
    data class DetalleSilo(val siloId: String) : Screen(Rutas.DETALLE_SILO) {
        override fun rutaNavegable(): String = "detalle_silo/$siloId"
    }

    data class RegistrarMovimiento(val siloId: String, val tipo: String? = null) :
        Screen(Rutas.REGISTRAR_MOVIMIENTO) {
        override fun rutaNavegable(): String =
            if (tipo != null) "registrar_movimiento/$siloId?tipo=$tipo" else "registrar_movimiento/$siloId"
    }

    data class ReportarIncidencia(val siloId: String) : Screen(Rutas.REPORTAR_INCIDENCIA) {
        override fun rutaNavegable(): String = "reportar_incidencia/$siloId"
    }
}
