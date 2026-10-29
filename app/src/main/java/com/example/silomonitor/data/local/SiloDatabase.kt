package com.example.silomonitor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.silomonitor.data.local.dao.AlertaDao
import com.example.silomonitor.data.local.dao.GranjaDao
import com.example.silomonitor.data.local.dao.IncidenciaDao
import com.example.silomonitor.data.local.dao.MovimientoDao
import com.example.silomonitor.data.local.dao.SiloDao
import com.example.silomonitor.data.local.dao.UsuarioDao
import com.example.silomonitor.data.local.entity.AlertaEntity
import com.example.silomonitor.data.local.entity.GranjaEntity
import com.example.silomonitor.data.local.entity.IncidenciaEntity
import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.data.local.entity.SiloEntity
import com.example.silomonitor.data.local.entity.UsuarioEntity

/**
 * Base de datos local (SQLite mediante Room). Permite consultar la última
 * información disponible aunque no haya conexión en la granja.
 */
@Database(
    entities = [
        GranjaEntity::class,
        SiloEntity::class,
        MovimientoEntity::class,
        AlertaEntity::class,
        IncidenciaEntity::class,
        UsuarioEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SiloDatabase : RoomDatabase() {

    abstract fun granjaDao(): GranjaDao
    abstract fun siloDao(): SiloDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun alertaDao(): AlertaDao
    abstract fun incidenciaDao(): IncidenciaDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var instancia: SiloDatabase? = null

        /** Singleton: una sola instancia de la base de datos para toda la app. */
        fun obtener(context: Context): SiloDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    SiloDatabase::class.java,
                    "silomonitor.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}
