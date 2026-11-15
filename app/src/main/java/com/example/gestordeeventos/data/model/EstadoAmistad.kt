package com.example.gestordeeventos.data.model

/**
 * Define los estados posibles para una relación de amistad o solicitud.
 * Utilizado en el módulo de gestión de amigos e invitaciones.
 */
object EstadoAmistad {
    const val PENDIENTE = "PENDIENTE" // Solicitud de amistad enviada, pendiente de aceptación
    const val ACEPTADA = "ACEPTADA"   // Solicitud aceptada, ya son amigos
}
