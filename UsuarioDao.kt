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

    // --- Issue 14: recuperar contraseña ---
    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("UPDATE usuarios SET claveHash = :claveHash WHERE id = :id")
    suspend fun actualizarClave(id: Long, claveHash: String)
}
