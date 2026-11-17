package com.example.gestordeeventos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Estados posibles de una amistad. */
object EstadoAmistad {
    const val PENDIENTE = "PENDIENTE" // solicitud enviada, falta que el otro la acepte
    const val ACEPTADA = "ACEPTADA"   // ya sois amigos
    const val BLOQUEADA = "BLOQUEADA"
}

/**
 * Tabla "amistades": cada fila es una pareja de usuarios que son amigos (relación muchos a muchos).
 *
 * La amistad es mutua, así que se guarda UNA sola fila por pareja, con el ID menor en `usuarioId`
 * y el mayor en `amigoId`. Así (2, 5) y (5, 2) son la misma amistad y no se puede duplicar.
 * Para crearla usa siempre [Amistad.entre], que ordena los IDs y rechaza la amistad de un usuario consigo mismo.
 *
 * Los nombres de usuario NO se copian aquí: ya existen (únicos) en "usuarios" y se obtienen con un JOIN
 * (ver [Amigo], más abajo, y `AmistadDao.listarAmigos`). Si se borra un usuario, se borran también sus amistades.
 */
@Entity(
    tableName = "amistades",
    primaryKeys = ["usuarioId", "amigoId"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["amigoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("amigoId")]
)
data class Amistad(
    val usuarioId: Long = 0L,
    val amigoId: Long = 0L,
    val solicitante: String = "",
    val destinatario: String = "",
    val estado: String = EstadoAmistad.ACEPTADA
) {
    init {
        if (usuarioId != 0L && amigoId != 0L) {
            require(usuarioId < amigoId) {
                "usuarioId debe ser menor que amigoId; usa Amistad.entre(a, b) para crear la amistad"
            }
        }
    }

    companion object {
        /** Crea la amistad entre dos usuarios, sin importar el orden en que se pasen sus IDs. */
        fun entre(a: Long, b: Long): Amistad {
            require(a != b) { "Un usuario no puede ser amigo de sí mismo" }
            return Amistad(
                usuarioId = minOf(a, b),
                amigoId = maxOf(a, b),
                solicitante = "",
                destinatario = "",
                estado = EstadoAmistad.ACEPTADA
            )
        }
    }
}

/**
 * Un amigo tal y como se muestra en la app: su ID y su nombre de usuario.
 * No es una tabla: es el resultado de unir "amistades" con "usuarios" (ver `AmistadDao.listarAmigos`).
 */
data class Amigo(
    val id: Long,
    val nombreUsuario: String
)
