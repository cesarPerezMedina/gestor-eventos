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
import com.example.gestordeeventos.data.repositorio.ResultadoLogin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Todo lo que necesita pintar la pantalla de login (Issue 10).
 * Los errores ya son texto listo para mostrar (null = no hay error).
 * Cuando [usuarioId] deja de ser null, el inicio de sesión ha ido bien y la pantalla puede navegar.
 */
data class LoginUiState(
    val identificador: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val errorIdentificador: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val usuarioId: Long? = null
) {
    val sesionIniciada: Boolean get() = usuarioId != null
}

/**
 * Lógica de la pantalla de login con correo o nombre de usuario y contraseña.
 * [texto] convierte el id de un recurso de strings.xml en su texto (así el ViewModel no necesita un Context).
 */
class LoginViewModel(
    private val repositorio: AuthRepositorio,
    private val texto: (Int) -> String
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onIdentificadorChange(valor: String) {
        _estado.update { it.copy(identificador = valor, errorIdentificador = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    /** Se llama al pulsar "Iniciar sesión". Valida que no haya campos vacíos y comprueba las credenciales. */
    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.cargando) return
        val errorIdentificador = if (actual.identificador.isBlank()) {
            texto(R.string.login_error_identificador_vacio)
        } else {
            null
        }
        val errorClave = if (actual.clave.isEmpty()) texto(R.string.login_error_clave_vacia) else null
        if (errorIdentificador != null || errorClave != null) {
            _estado.update { it.copy(errorIdentificador = errorIdentificador, errorClave = errorClave) }
        } else {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            viewModelScope.launch { comprobarCredenciales(actual.identificador, actual.clave) }
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun comprobarCredenciales(identificador: String, clave: String) {
        val resultado = try {
            repositorio.iniciarSesion(identificador, clave)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null // error inesperado (por ejemplo, de la base de datos): se muestra un mensaje genérico
        }
        _estado.update {
            when (resultado) {
                is ResultadoLogin.Exito -> it.copy(cargando = false, clave = "", usuarioId = resultado.usuario.id)
                ResultadoLogin.CredencialesIncorrectas ->
                    it.copy(cargando = false, errorGeneral = texto(R.string.login_error_credenciales))
                null -> it.copy(cargando = false, errorGeneral = texto(R.string.login_error_generico))
            }
        }
    }

    companion object {
        /** Fábrica para Compose: `viewModel(factory = LoginViewModel.Factory)`. */
        @Suppress("ktlint:standard:property-naming")
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[APPLICATION_KEY] as Application
                LoginViewModel(
                    repositorio = AuthRepositorio(AppDatabase.obtener(aplicacion).usuarioDao()),
                    texto = { id -> aplicacion.getString(id) }
                )
            }
        }
    }
}
