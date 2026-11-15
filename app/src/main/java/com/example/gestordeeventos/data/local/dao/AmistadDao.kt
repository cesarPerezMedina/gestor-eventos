package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.data.model.Amistad
import kotlinx.coroutines.flow.Flow

/**
 * Consultas sobre "amistades". Las funciones que reciben dos IDs funcionan en cualquier orden,
 * porque la amistad es mutua.
 */
@Dao
interface AmistadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarRaw(amistad: Amistad): Long

    @Query("SELECT id FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerIdPorCorreo(correo: String): Long?

    /** Guarda la amistad o solicitud. Devuelve -1 si ya existía (no falla ni se duplica). */
    suspend fun insertar(amistad: Amistad): Long {
        if (amistad.usuarioId != 0L && amistad.amigoId != 0L) {
            return insertarRaw(amistad)
        }
        if (amistad.solicitante.isEmpty() || amistad.destinatario.isEmpty()) {
            return -1L
        }
        val idSol = obtenerIdPorCorreo(amistad.solicitante) ?: return -1L
        val idDes = obtenerIdPorCorreo(amistad.destinatario) ?: return -1L
        if (idSol == idDes) return -1L
        val nueva = Amistad(
            usuarioId = minOf(idSol, idDes),
            amigoId = maxOf(idSol, idDes),
            solicitante = amistad.solicitante,
            destinatario = amistad.destinatario,
            estado = amistad.estado
        )
        return insertarRaw(nueva)
    }

    @Query(
        "SELECT EXISTS(SELECT 1 FROM amistades " +
            "WHERE ((usuarioId = :a AND amigoId = :b) OR (usuarioId = :b AND amigoId = :a)) AND (estado = 'ACEPTADA' OR estado = ''))"
    )
    suspend fun sonAmigos(a: Long, b: Long): Boolean

    @Query(
        "SELECT a.* FROM amistades a " +
            "JOIN usuarios u1 ON (a.usuarioId = u1.id OR a.amigoId = u1.id) " +
            "JOIN usuarios u2 ON (a.usuarioId = u2.id OR a.amigoId = u2.id) " +
            "WHERE ((u1.correo = :c1 AND u2.correo = :c2) OR (u1.correo = :c2 AND u2.correo = :c1)) " +
            "AND u1.id != u2.id LIMIT 1"
    )
    suspend fun entre(c1: String, c2: String): Amistad?

    @Query(
        "UPDATE amistades SET estado = 'ACEPTADA' " +
            "WHERE solicitante = :solicitante AND destinatario = :destinatario AND estado = 'PENDIENTE'"
    )
    suspend fun aceptar(solicitante: String, destinatario: String): Int

    @Query(
        "DELETE FROM amistades " +
            "WHERE solicitante = :solicitante AND destinatario = :destinatario AND estado = 'PENDIENTE'"
    )
    suspend fun eliminarPendiente(solicitante: String, destinatario: String): Int

    @Query("SELECT * FROM amistades WHERE destinatario = :miCorreo AND estado = 'PENDIENTE'")
    suspend fun solicitudesRecibidas(miCorreo: String): List<Amistad>

    /** Amigos de un usuario con su ID y nombre de usuario, ordenados alfabéticamente. */
    @Query(
        "SELECT u.id AS id, u.nombreUsuario AS nombreUsuario FROM usuarios u " +
            "INNER JOIN amistades a ON " +
            "((a.usuarioId = :usuarioId AND u.id = a.amigoId) OR (a.amigoId = :usuarioId AND u.id = a.usuarioId)) " +
            "WHERE (a.estado = 'ACEPTADA' OR a.estado = '') " +
            "ORDER BY u.nombreUsuario COLLATE NOCASE"
    )
    suspend fun listarAmigos(usuarioId: Long): List<Amigo>

    @Query("SELECT COUNT(*) FROM amistades WHERE (usuarioId = :usuarioId OR amigoId = :usuarioId) AND (estado = 'ACEPTADA' OR estado = '')")
    suspend fun contarAmigos(usuarioId: Long): Int

    /** Elimina la amistad. Devuelve cuántas filas se borraron (0 si no eran amigos). */
    @Query(
        "DELETE FROM amistades " +
            "WHERE (usuarioId = :a AND amigoId = :b) OR (usuarioId = :b AND amigoId = :a)"
    )
    suspend fun eliminar(a: Long, b: Long): Int
    /** Marca la amistad como bloqueada por [yo]. Devuelve las filas cambiadas (0 si no existía). */
    @Query(
        "UPDATE amistades SET estado = 'BLOQUEADA', " +
            "solicitante = (SELECT correo FROM usuarios WHERE id = :yo), " +
            "destinatario = (SELECT correo FROM usuarios WHERE id = :amigo) " +
            "WHERE (usuarioId = :yo AND amigoId = :amigo) OR (usuarioId = :amigo AND amigoId = :yo)"
    )
    suspend fun bloquear(yo: Long, amigo: Long): Int


    /** Amigos aceptados de un usuario (por correo). Room la vuelve a emitir cada vez que cambian las tablas. */
    @Query(
        "SELECT u.id AS id, u.nombreUsuario AS nombreUsuario FROM usuarios u " +
            "INNER JOIN usuarios yo ON yo.correo = :miCorreo " +
            "INNER JOIN amistades a ON " +
            "((a.usuarioId = yo.id AND a.amigoId = u.id) OR (a.amigoId = yo.id AND a.usuarioId = u.id)) " +
            "WHERE (a.estado = 'ACEPTADA' OR a.estado = '') " +
            "ORDER BY u.nombreUsuario COLLATE NOCASE"
    )
    fun observarAmigos(miCorreo: String): Flow<List<Amigo>>
}
