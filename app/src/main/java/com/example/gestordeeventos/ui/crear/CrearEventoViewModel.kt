
package com.example.gestordeeventos.ui.crear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import kotlinx.coroutines.launch

/** Lo que el usuario ha rellenado en la pantalla "Crear evento". */
data class NuevoEvento(
    val nombre: String,
    val descripcion: String,
    val deporte: Deporte?,
    val publico: Boolean,
    val minJugadores: Int,
    val maxJugadores: Int,
    val ubicacion: String,
    val invitadosIds: List<String>,
    val indicaciones: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val inicioMillis: Long = System.currentTimeMillis(),
    val finMillis: Long = 0L,
)

/**
 * @param miId devuelve el id del usuario con sesión iniciada (o null si no hay sesión).
 */
class CrearEventoViewModel(
    private val repositorio: EventoRepositorio,
    private val miId: suspend () -> Long?,
) : ViewModel() {

    /** Botón "Crear evento deportivo". La visibilidad (público / privado) viene de [NuevoEvento.publico]. */
    fun crearEvento(nuevo: NuevoEvento, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        viewModelScope.launch {
            val yo = miId()
            if (yo == null) {
                onResultado(false, "Inicia sesión para crear un evento")
                return@launch
            }

            val evento = Evento(
                nombre = nuevo.nombre,
                descripcion = nuevo.descripcion,
                deporte = nuevo.deporte ?: Deporte.OTROS,
                publico = nuevo.publico,
                minParticipantes = nuevo.minJugadores,
                maxParticipantes = nuevo.maxJugadores,
                latitud = nuevo.latitud ?: 0.0,
                longitud = nuevo.longitud ?: 0.0,
                // Evento solo tiene un campo de texto para el lugar: si hay indicaciones, van detrás
                direccion = listOf(nuevo.ubicacion, nuevo.indicaciones).filter { it.isNotBlank() }.joinToString(" · "),
                // PENDIENTE: la hora de fin (nuevo.finMillis) todavía no tiene columna en Evento
                fechaHora = nuevo.inicioMillis,
                creadorId = yo,
            )
            val resultado = repositorio.crearEvento(evento, nuevo.invitadosIds.mapNotNull { it.toLongOrNull() })
            onResultado(resultado.ok, resultado.mensaje)
        }
    }
}

class CrearEventoViewModelFactory(
    private val repositorio: EventoRepositorio,
    private val miId: suspend () -> Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CrearEventoViewModel(repositorio, miId) as T
}
 
