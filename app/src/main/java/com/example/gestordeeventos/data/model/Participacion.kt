package com.example.gestordeeventos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Tabla "participaciones": qué usuario participa en qué evento (relación muchos a muchos).
 * Si se borra el evento o el usuario, se borran también sus participaciones.
 */
@Entity(
    tableName = "participaciones",
    primaryKeys = ["eventoId", "usuarioId"],
    foreignKeys = [
        ForeignKey(entity = Evento::class, parentColumns = ["id"], childColumns = ["eventoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("usuarioId")]
)
data class Participacion(
    val eventoId: Long,
    val usuarioId: Long
)
