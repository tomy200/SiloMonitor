package com.example.silomonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.silomonitor.data.local.entity.GranjaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GranjaDao {

    @Query("SELECT * FROM granjas ORDER BY id")
    fun observarTodas(): Flow<List<GranjaEntity>>

    @Query("SELECT * FROM granjas WHERE id = :id")
    suspend fun obtener(id: Int): GranjaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(granjas: List<GranjaEntity>)
}
