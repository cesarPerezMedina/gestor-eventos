package com.example.gestordeeventos.data.model

/**
 * Una invitación recibida que todavía no se ha aceptado ni rechazado, con los datos
 * que necesita la pantalla de invitaciones (Issue 71): qué evento es y quién invita.
 * No es una tabla: la rellena la consulta de [com.example.gestordeeventos.data.local.dao.InvitacionDao].
 */
data class InvitacionPendiente(
    val invitacionId: Long,
    val eventoId: Long,
    val eventoNombre: String,
    val deporte: Deporte,
    val fechaHora: Long,
    val deUsuarioId: Long,
    val deNombreUsuario: String
)
