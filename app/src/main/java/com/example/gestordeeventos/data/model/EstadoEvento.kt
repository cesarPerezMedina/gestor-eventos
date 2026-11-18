package com.example.gestordeeventos.data.model

/**
 * Estado de un evento. Room lo guarda como texto con el nombre (p. ej. "PUBLICADO").
 * - PUBLICADO: evento normal; solo estos salen en Buscar.
 * - BORRADOR: lo está preparando su creador y todavía no se ve para los demás.
 * - CANCELADO: el creador lo ha cancelado; sigue en "Mis eventos" pero no se puede apuntar nadie.
 */
enum class EstadoEvento { PUBLICADO, BORRADOR, CANCELADO,ARCHIVADO }
const val HORAS_HASTA_ARCHIVAR = 48L
