package com.example.gestordeeventos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "eventos".
 * - fechaHora: milisegundos desde 1970 (System.currentTimeMillis()).
 * - creadorId: usuario que lo organiza; si ese usuario se borra, se borran sus eventos.
 * Quién participa se guarda en la tabla "participaciones" (ver [Participacion]).
 */
@Entity(
    tableName = "eventos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["creadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creadorId")]
)
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    val deporte: Deporte = Deporte.OTROS,
    val publico: Boolean = true,
    val minParticipantes: Int = 0,
    val maxParticipantes: Int = 0,
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fechaHora: Long,
    val creadorId: Long
)
