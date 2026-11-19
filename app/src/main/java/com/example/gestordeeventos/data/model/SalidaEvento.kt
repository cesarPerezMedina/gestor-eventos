package com.example.gestordeeventos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Tabla "salidas": eventos a los que un usuario se había unido y que ha abandonado.
 *
 * Al salir de un evento se borra su participación (libera la plaza); esta fila deja constancia de que salió,
 * para que el evento siga saliéndole en "Mis eventos > Unidos > Cancelados" como "Abandonado".
 * Si vuelve a apuntarse, la fila se borra. Si se borra el evento o el usuario, se borra también la salida.
 */
@Entity(
    tableName = "salidas",
    primaryKeys = ["eventoId", "usuarioId"],
    foreignKeys = [
        ForeignKey(entity = Evento::class, parentColumns = ["id"], childColumns = ["eventoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("usuarioId")]
)
data class SalidaEvento(
    val eventoId: Long,
    val usuarioId: Long,
    /** Cuándo salió (milisegundos). */
    val fecha: Long = System.currentTimeMillis()
)
