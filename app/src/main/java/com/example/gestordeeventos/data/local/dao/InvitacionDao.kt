package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestordeeventos.data.model.Invitacion

/** Consultas sobre "invitaciones". */
@Dao
interface InvitacionDao {
    @Insert
    suspend fun insertar(invitacion: Invitacion): Long

    @Query("SELECT * FROM invitaciones WHERE id = :id")
    suspend fun buscarPorId(id: Long): Invitacion?
}
