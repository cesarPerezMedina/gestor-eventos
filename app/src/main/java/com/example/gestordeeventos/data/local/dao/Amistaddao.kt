package com.example.gestordeeventos.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gestordeeventos.data.model.Amistad

@Dao
interface AmistadDao {

    /** Guarda la solicitud. Si ya existía exactamente la misma, no hace nada. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(amistad: Amistad): Long

    /** Busca si ya hay algo entre dos usuarios, en cualquiera de las dos direcciones. */
    @Query(
        "SELECT * FROM amistad " +
                "WHERE (solicitante = :a AND destinatario = :b) OR (solicitante = :b AND destinatario = :a) " +
                "LIMIT 1"
    )
    suspend fun entre(a: String, b: String): Amistad?

    /** Solicitudes pendientes que ha recibido un usuario. */
    @Query("SELECT * FROM amistad WHERE destinatario = :correo AND estado = 'PENDIENTE' ORDER BY fecha DESC")
    suspend fun solicitudesRecibidas(correo: String): List<Amistad>

    /** Acepta una solicitud pendiente. Devuelve cuántas filas ha cambiado (0 si ya no existe). */
    @Query(
        "UPDATE amistad SET estado = 'ACEPTADA' " +
                "WHERE solicitante = :solicitante AND destinatario = :destinatario AND estado = 'PENDIENTE'"
    )
    suspend fun aceptar(solicitante: String, destinatario: String): Int

    /** Borra una solicitud pendiente (rechazarla). Devuelve cuántas filas ha borrado. */
    @Query(
        "DELETE FROM amistad " +
                "WHERE solicitante = :solicitante AND destinatario = :destinatario AND estado = 'PENDIENTE'"
    )
    suspend fun eliminarPendiente(solicitante: String, destinatario: String): Int
}