package com.example.gestordeeventos

import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.ui.auth.LoginViewModel
import com.example.gestordeeventos.ui.auth.RegistroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Flujo completo: registrarse con `RegistroViewModel` y entrar después con `LoginViewModel` (Issue 18). */
@OptIn(ExperimentalCoroutinesApi::class)
class AutenticacionFlujoTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepositorio(dao, dispatcher)

    /** Texto falso para los recursos: así los tests no dependen del idioma. */
    private fun texto(id: Int) = "texto:$id"

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        val registro = RegistroViewModel(repositorio, this::texto)
        registro.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
        assertTrue(registro.estado.value.registroCompletado)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun nuevoLogin() = LoginViewModel(repositorio, this::texto)

    @Test
    fun trasRegistrarseSePuedeEntrarConElCorreo() {
        val login = nuevoLogin()
        login.iniciarSesion("ana@correo.com", "Clave#2026")
        assertTrue(login.estado.value.sesionIniciada)
        assertEquals(dao.guardados.single().id, login.estado.value.usuarioId)
    }

    @Test
    fun trasRegistrarseSePuedeEntrarConElNombreDeUsuario() {
        val login = nuevoLogin()
        login.iniciarSesion("ana_23", "Clave#2026")
        assertTrue(login.estado.value.sesionIniciada)
    }

    @Test
    fun trasRegistrarseUnaClaveIncorrectaNoDejaEntrar() {
        val login = nuevoLogin()
        login.iniciarSesion("ana_23", "Otra#Clave2026")
        assertFalse(login.estado.value.sesionIniciada)
        assertEquals(texto(R.string.login_error_credenciales), login.estado.value.errorGeneral)
    }

    @Test
    fun noSePuedeRegistrarDeNuevoElMismoUsuarioPeroSigueEntrando() {
        val registro = RegistroViewModel(repositorio, this::texto)
        registro.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
        assertFalse(registro.estado.value.registroCompletado)
        assertEquals(1, dao.guardados.size)

        val login = nuevoLogin()
        login.iniciarSesion("ana@correo.com", "Clave#2026")
        assertTrue(login.estado.value.sesionIniciada)
    }
}
