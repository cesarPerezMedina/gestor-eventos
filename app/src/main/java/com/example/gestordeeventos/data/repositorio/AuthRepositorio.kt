package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.util.ClaveHash
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resultado de intentar iniciar sesión.
 * No se distingue "el usuario no existe" de "contraseña incorrecta" para no revelar qué usuarios están registrados.
 */
sealed class ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin()
    data object CredencialesIncorrectas : ResultadoLogin()
}

/** Resultado de intentar registrar un usuario nuevo. */
sealed class ResultadoRegistro {
    data class Exito(val usuario: Usuario) : ResultadoRegistro()
    data object CorreoEnUso : ResultadoRegistro()
    data object NombreUsuarioEnUso : ResultadoRegistro()
}

/**
 * Operaciones de autenticación sobre la base de datos local.
 * [dispatcher] es donde se calcula el hash (costoso); en los tests se sustituye por uno de pruebas.
 */
class AuthRepositorio(
    private val usuarioDao: UsuarioDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    /** Inicia sesión con el correo o el nombre de usuario ([identificador]) y la contraseña. */
    suspend fun iniciarSesion(identificador: String, clave: String): ResultadoLogin {
        val usuario = usuarioDao.buscarPorCorreoONombreUsuario(identificador.trim())
        return when {
            usuario == null -> ResultadoLogin.CredencialesIncorrectas
            withContext(dispatcher) { ClaveHash.verificar(clave, usuario.claveHash) } -> ResultadoLogin.Exito(usuario)
            else -> ResultadoLogin.CredencialesIncorrectas
        }
    }

    /**
     * Registra un usuario nuevo (Issue 13). Los datos deben llegar ya validados (ver `ValidadorRegistro`).
     * El correo se guarda en minúsculas y sin espacios; la contraseña se guarda solo como hash.
     * Si el correo o el nombre de usuario ya existen (sin distinguir mayúsculas), no se guarda nada.
     */
    suspend fun registrar(nombreUsuario: String, correo: String, clave: String): ResultadoRegistro {
        val nombre = nombreUsuario.trim()
        val correoNormalizado = correo.trim().lowercase()
        return when {
            usuarioDao.existeCorreo(correoNormalizado) -> ResultadoRegistro.CorreoEnUso
            usuarioDao.existeNombreUsuario(nombre) -> ResultadoRegistro.NombreUsuarioEnUso
            else -> {
                val claveHash = withContext(dispatcher) { ClaveHash.generar(clave) }
                val nuevo = Usuario(nombreUsuario = nombre, correo = correoNormalizado, claveHash = claveHash)
                ResultadoRegistro.Exito(nuevo.copy(id = usuarioDao.insertar(nuevo)))
            }
        }
    }
}
