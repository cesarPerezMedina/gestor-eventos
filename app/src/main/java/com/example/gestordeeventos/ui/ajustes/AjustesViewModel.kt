package com.example.gestordeeventos.ui.ajustes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EscalaTexto
import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.data.repositorio.AjustesRepositorio
import kotlinx.coroutines.flow.StateFlow

/** Estado y acciones de la pantalla de Ajustes. */
class AjustesViewModel(private val repositorio: AjustesRepositorio) : ViewModel() {

    /** Aspecto elegido actualmente. */
    val modoTema: StateFlow<ModoTema> = repositorio.modoTema

    /** Tamaño de texto elegido actualmente. */
    val escalaTexto: StateFlow<EscalaTexto> = repositorio.escalaTexto

    /** Radio de búsqueda de partidos, en km. */
    val radioBusquedaKm: StateFlow<Int> = repositorio.radioBusquedaKm

    /** Deportes marcados como favoritos. */
    val deportesFavoritos: StateFlow<Set<Deporte>> = repositorio.deportesFavoritos

    val recordatorioPartidos: StateFlow<Boolean> = repositorio.recordatorioPartidos

    val avisosPlazasVacantes: StateFlow<Boolean> = repositorio.avisosPlazasVacantes

    val invitacionesDirectas: StateFlow<Boolean> = repositorio.invitacionesDirectas

    fun elegirModoTema(modo: ModoTema) = repositorio.cambiarModoTema(modo)

    fun elegirEscalaTexto(escala: EscalaTexto) = repositorio.cambiarEscalaTexto(escala)

    fun elegirRadioBusquedaKm(km: Int) = repositorio.cambiarRadioBusquedaKm(km)

    fun alternarDeporteFavorito(deporte: Deporte) = repositorio.alternarDeporteFavorito(deporte)

    fun cambiarRecordatorioPartidos(activo: Boolean) = repositorio.cambiarRecordatorioPartidos(activo)

    fun cambiarAvisosPlazasVacantes(activo: Boolean) = repositorio.cambiarAvisosPlazasVacantes(activo)

    fun cambiarInvitacionesDirectas(activo: Boolean) = repositorio.cambiarInvitacionesDirectas(activo)

    companion object {
        /** Fábrica para Compose: `viewModel(factory = AjustesViewModel.Factory)`. */
        @Suppress("ktlint:standard:property-naming")
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[APPLICATION_KEY] as Application
                AjustesViewModel(AjustesRepositorio.obtener(aplicacion))
            }
        }
    }
}