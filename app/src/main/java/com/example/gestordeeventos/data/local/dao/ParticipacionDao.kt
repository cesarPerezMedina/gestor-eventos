package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestordeeventos.data.model.Participacion

/** Consultas sobre "participaciones" (quién se ha unido a qué evento). */
@Dao
interface ParticipacionDao {
    @Insert
    suspend fun insertar(participacion: Participacion)

    @Query("SELECT COUNT(*) FROM participaciones WHERE eventoId = :eventoId")
    suspend fun contarDeEvento(eventoId: Long): Int
}
