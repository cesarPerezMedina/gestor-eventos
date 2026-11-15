package com.example.gestordeeventos.ui.ajustes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.data.repositorio.AjustesRepositorio
import kotlinx.coroutines.flow.StateFlow

/** Estado y acciones de la pantalla de Ajustes. */
class AjustesViewModel(private val repositorio: AjustesRepositorio) : ViewModel() {

    /** Aspecto elegido actualmente. */
    val modoTema: StateFlow<ModoTema> = repositorio.modoTema

    fun elegirModoTema(modo: ModoTema) = repositorio.cambiarModoTema(modo)

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
