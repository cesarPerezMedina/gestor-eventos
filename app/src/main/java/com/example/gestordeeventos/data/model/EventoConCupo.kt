package com.example.gestordeeventos.data.model

import androidx.room.Embedded


data class EventoConCupo(
    @Embedded val evento: Evento,
    val apuntados: Int,
)
