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

    /**
     * Devuelve todos los eventos creados por el usuario indicado.
     * Se ordenan por fecha, de más próximos a más lejanos.
     */
    @Query("SELECT * FROM eventos WHERE creadorId = :usuarioId ORDER BY fechaHora ASC")
    suspend fun listarCreadosPorUsuario(usuarioId: Long): List<Evento>

    /**
     * Devuelve los eventos en los que el usuario tiene una participación.
     * El JOIN evita depender de que el usuario sea también el creador.
     * Se ordenan por fecha, de más próximos a más lejanos.
     */
    @Query(
        "SELECT e.* FROM eventos e " +
            "INNER JOIN participaciones p ON e.id = p.eventoId " +
            "WHERE p.usuarioId = :usuarioId " +
            "ORDER BY e.fechaHora ASC"
    )
    suspend fun listarEventosParticipadosPorUsuario(usuarioId: Long): List<Evento>
}
