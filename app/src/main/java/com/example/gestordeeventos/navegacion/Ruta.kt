package com.example.gestordeeventos.navegacion

/** Nombre del argumento con el ID del evento en la ruta de detalle. */
const val ARG_EVENTO_ID = "eventoId"

/**
 * Todas las pantallas a las que se puede navegar.
 *
 * - Autenticación: [Login], [Registro] y [Recuperar]. Se ven a pantalla completa, sin barra inferior.
 * - Pestañas de la barra inferior: [MisEventos], [Buscar], [Crear], [Amigos] y [Ajustes]
 *   (ver `BarraInferior`).
 * - [DetalleEvento] no es una pestaña: se abre encima de ellas y la barra inferior se oculta.
 */
sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Recuperar : Ruta("recuperar")

    data object MisEventos : Ruta("mis_eventos")
    data object Buscar : Ruta("buscar")
    data object Crear : Ruta("crear")
    data object Amigos : Ruta("amigos")
    data object Ajustes : Ruta("ajustes")

    data object DetalleEvento : Ruta("evento/{$ARG_EVENTO_ID}") {
        /** Ruta concreta para abrir un evento: `navController.navigate(Ruta.DetalleEvento.crear(id))`. */
        fun crear(eventoId: Long): String = "evento/$eventoId"
    }
}
