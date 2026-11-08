package com.example.gestordeeventos.ui.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoRegistro
import com.example.gestordeeventos.util.ValidadorRegistro
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Todo lo que necesita pintar la pantalla de registro (Issue 13).
 * Los errores ya son texto listo para mostrar (null = no hay error).
 * Cuando [registroCompletado] es true, el usuario se ha guardado y la pantalla puede navegar.
 */
data class RegistroUiState(
    val nombreUsuario: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val cargando: Boolean = false,
    val errorNombreUsuario: String? = null,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null,
    val registroCompletado: Boolean = false
)

/**
 * Lógica de la pantalla de registro: valida los campos con [ValidadorRegistro] y guarda el usuario
 * en la base de datos.
 * [texto] convierte el id de un recurso de strings.xml en su texto (así el ViewModel no necesita un Context).
 */
class RegistroViewModel(
    private val repositorio: AuthRepositorio,
    private val texto: (Int) -> String
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    fun onNombreUsuarioChange(valor: String) {
        _estado.update { it.copy(nombreUsuario = valor, errorNombreUsuario = null, errorGeneral = null) }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errorClave = null, errorConfirmacion = null, errorGeneral = null) }
    }

    fun onConfirmacionChange(valor: String) {
        _estado.update { it.copy(confirmacion = valor, errorConfirmacion = null, errorGeneral = null) }
    }

    /** Se llama al pulsar "Crear cuenta": valida todos los campos y, si son correctos, registra al usuario. */
    fun registrar() {
        val actual = _estado.value
        if (actual.cargando) return
        val errores = validar(actual)
        if (errores.hayErrores) {
            _estado.update {
                it.copy(
                    errorNombreUsuario = errores.nombreUsuario,
                    errorCorreo = errores.correo,
                    errorClave = errores.clave,
                    errorConfirmacion = errores.confirmacion,
                    errorGeneral = null
                )
            }
        } else {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            viewModelScope.launch { guardar(actual) }
        }
    }

    private class Errores(
        val nombreUsuario: String?,
        val correo: String?,
        val clave: String?,
        val confirmacion: String?
    ) {
        val hayErrores: Boolean get() = listOf(nombreUsuario, correo, clave, confirmacion).any { it != null }
    }

    private fun validar(estado: RegistroUiState) = Errores(
        nombreUsuario = ValidadorRegistro.validarNombreUsuario(estado.nombreUsuario).mensajeError,
        correo = ValidadorRegistro.validarCorreo(estado.correo).mensajeError,
        clave = ValidadorRegistro.validarPassword(estado.clave).mensajeError,
        confirmacion = ValidadorRegistro.validarConfirmacionPassword(estado.clave, estado.confirmacion).mensajeError
    )

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun guardar(datos: RegistroUiState) {
        val resultado = try {
            repositorio.registrar(datos.nombreUsuario, datos.correo, datos.clave)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null // error inesperado (por ejemplo, de la base de datos): se muestra un mensaje genérico
        }
        _estado.update {
            when (resultado) {
                is ResultadoRegistro.Exito ->
                    it.copy(cargando = false, clave = "", confirmacion = "", registroCompletado = true)
                ResultadoRegistro.CorreoEnUso ->
                    it.copy(cargando = false, errorCorreo = texto(R.string.registro_error_correo_en_uso))
                ResultadoRegistro.NombreUsuarioEnUso ->
                    it.copy(cargando = false, errorNombreUsuario = texto(R.string.registro_error_nombre_en_uso))
                null -> it.copy(cargando = false, errorGeneral = texto(R.string.registro_error_generico))
            }
        }
    }

    companion object {
        /** Fábrica para Compose: `viewModel(factory = RegistroViewModel.Factory)`. */
        @Suppress("ktlint:standard:property-naming")
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[APPLICATION_KEY] as Application
                RegistroViewModel(
                    repositorio = AuthRepositorio(AppDatabase.obtener(aplicacion).usuarioDao()),
                    texto = { id -> aplicacion.getString(id) }
                )
            }
        }
    }
}
