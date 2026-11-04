package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestordeeventos.data.model.Evento

/** Consultas sobre "eventos". Cada Issue añade aquí las que necesite (búsqueda, listados, borrado...). */
@Dao
interface EventoDao {
    @Insert
    suspend fun insertar(evento: Evento): Long

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Evento?
}
