package com.example.silomonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.silomonitor.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    fun observar(correo: String): Flow<UsuarioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)

    /** Registro de una cuenta nueva: falla si el correo ya existe (no reemplaza a nadie). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarNuevo(usuario: UsuarioEntity)

    @Query("UPDATE usuarios SET fotoUri = :fotoUri WHERE correo = :correo")
    suspend fun actualizarFoto(correo: String, fotoUri: String?)
}
