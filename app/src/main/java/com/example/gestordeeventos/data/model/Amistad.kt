package com.example.gestordeeventos.data.model

import androidx.room.Entity

/** Estados posibles de una amistad. */
object EstadoAmistad {
    const val PENDIENTE = "PENDIENTE" // solicitud enviada, falta que el otro la acepte
    const val ACEPTADA = "ACEPTADA"   // ya sois amigos
}

/**
 * Una fila = una solicitud de amistad (o una amistad ya aceptada).
 * Los usuarios se identifican por su correo.
 *
 * - solicitante: quien envía la solicitud
 * - destinatario: quien la recibe y puede aceptarla o rechazarla
 */
@Entity(tableName = "amistad", primaryKeys = ["solicitante", "destinatario"])
data class Amistad(
    val solicitante: String,
    val destinatario: String,
    val estado: String = EstadoAmistad.PENDIENTE,
    val fecha: Long = System.currentTimeMillis(),
)
