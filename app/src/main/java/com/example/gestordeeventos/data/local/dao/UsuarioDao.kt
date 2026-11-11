package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.model.UsuarioEncontrado

/** Consultas sobre "usuarios". Cada Issue añade aquí las que necesite (login, registro, búsqueda...). */
@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): Usuario?

    /**
     * Busca un usuario por su correo o por su nombre de usuario, sin distinguir mayúsculas
     * (es lo que se escribe en el campo único del login). Devuelve null si no existe.
     */
    @Query(
        "SELECT * FROM usuarios WHERE correo = :identificador COLLATE NOCASE " +
            "OR nombreUsuario = :identificador COLLATE NOCASE LIMIT 1"
    )
    suspend fun buscarPorCorreoONombreUsuario(identificador: String): Usuario?

    /** true si ya hay un usuario con ese correo (sin distinguir mayúsculas). */
    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE correo = :correo COLLATE NOCASE)")
    suspend fun existeCorreo(correo: String): Boolean

    /** true si ya hay un usuario con ese nombre de usuario (sin distinguir mayúsculas). */
    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE nombreUsuario = :nombreUsuario COLLATE NOCASE)")
    suspend fun existeNombreUsuario(nombreUsuario: String): Boolean

    /**
     * Busca usuarios por ID exacto o por parte del nombre de usuario (Issue 31).
     *
     * - [id]: ID que se ha escrito, o null si el texto no era un número. Ese usuario sale el primero.
     * - [patron]: texto a buscar dentro del nombre, YA ESCAPADO para LIKE (la barra invertida escapa).
     *   Usa `AmigosRepositorio.buscarUsuarios`, que lo prepara. LIKE no distingue mayúsculas en ASCII.
     * - Nunca devuelve al propio [usuarioActualId]. Orden: el ID exacto, luego los nombres que empiezan
     *   por el texto, luego el resto, cada grupo por orden alfabético. Como mucho [limite] resultados.
     */
    @Query(
        "SELECT u.id AS id, u.nombreUsuario AS nombreUsuario, " +
            "EXISTS(SELECT 1 FROM amistades a WHERE " +
            "(a.usuarioId = :usuarioActualId AND a.amigoId = u.id) OR " +
            "(a.amigoId = :usuarioActualId AND a.usuarioId = u.id)) AS yaEsAmigo " +
            "FROM usuarios u " +
            "WHERE u.id != :usuarioActualId " +
            "AND (u.id = :id OR u.nombreUsuario LIKE '%' || :patron || '%' ESCAPE '\\') " +
            "ORDER BY CASE WHEN u.id = :id THEN 0 " +
            "WHEN u.nombreUsuario LIKE :patron || '%' ESCAPE '\\' THEN 1 ELSE 2 END, " +
            "u.nombreUsuario COLLATE NOCASE " +
            "LIMIT :limite"
    )
    suspend fun buscarUsuarios(usuarioActualId: Long, id: Long?, patron: String, limite: Int): List<UsuarioEncontrado>
}
