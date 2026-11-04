package com.example.gestordeeventos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EstadoInvitacion { PENDIENTE, ACEPTADA, RECHAZADA }

/** Tabla "invitaciones": un usuario (de) invita a otro (para) a un evento. */
@Entity(
    tableName = "invitaciones",
    foreignKeys = [
        ForeignKey(entity = Evento::class, parentColumns = ["id"], childColumns = ["eventoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["deUsuarioId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["paraUsuarioId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventoId"), Index("deUsuarioId"), Index("paraUsuarioId")]
)
data class Invitacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val deUsuarioId: Long,
    val paraUsuarioId: Long,
    val estado: EstadoInvitacion = EstadoInvitacion.PENDIENTE
)
