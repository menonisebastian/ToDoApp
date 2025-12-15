package com.example.todoapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao
{
    @Query("SELECT * FROM tareas WHERE completada = 0 ORDER BY fecha, id")
    fun obtenerTodas(): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE completada = 1 ORDER BY fecha, id")
    fun obtenerCompletadas(): Flow<List<Tarea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: Tarea)

    @Update
    suspend fun actualizar(tarea: Tarea)

    @Delete
    suspend fun eliminar(tarea: Tarea)

    @Query("DELETE FROM tareas")
    suspend fun eliminarTodas()
}