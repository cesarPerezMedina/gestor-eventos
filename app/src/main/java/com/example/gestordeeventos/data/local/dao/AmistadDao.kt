package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.data.model.Amistad

/**
 * Consultas sobre "amistades". Las funciones que reciben dos IDs funcionan en cualquier orden,
 * porque la amistad es mutua.
 */
@Dao
interface AmistadDao {
    /** Guarda la amistad. Devuelve -1 si ya existía (no falla ni se duplica). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(amistad: Amistad): Long

    @Query(
        "SELECT EXISTS(SELECT 1 FROM amistades " +
            "WHERE (usuarioId = :a AND amigoId = :b) OR (usuarioId = :b AND amigoId = :a))"
    )
    suspend fun sonAmigos(a: Long, b: Long): Boolean

    /** Amigos de un usuario con su ID y nombre de usuario, ordenados alfabéticamente. */
    @Query(
        "SELECT u.id AS id, u.nombreUsuario AS nombreUsuario FROM usuarios u " +
            "INNER JOIN amistades a ON " +
            "(a.usuarioId = :usuarioId AND u.id = a.amigoId) OR (a.amigoId = :usuarioId AND u.id = a.usuarioId) " +
            "ORDER BY u.nombreUsuario COLLATE NOCASE"
    )
    suspend fun listarAmigos(usuarioId: Long): List<Amigo>

    @Query("SELECT COUNT(*) FROM amistades WHERE usuarioId = :usuarioId OR amigoId = :usuarioId")
    suspend fun contarAmigos(usuarioId: Long): Int

    /** Elimina la amistad. Devuelve cuántas filas se borraron (0 si no eran amigos). */
    @Query(
        "DELETE FROM amistades " +
            "WHERE (usuarioId = :a AND amigoId = :b) OR (usuarioId = :b AND amigoId = :a)"
    )
    suspend fun eliminar(a: Long, b: Long): Int
}
