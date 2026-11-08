package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestordeeventos.data.model.Usuario

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
}
