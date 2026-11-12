package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.ui.auth.LoginViewModel
import com.example.gestordeeventos.util.ClaveHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dao = FakeUsuarioDao()
    private lateinit var viewModel: LoginViewModel

    /** Texto falso para los recursos: así los tests no dependen del idioma. */
    private fun texto(id: Int) = "texto:$id"

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        runBlocking {
            val claveHash = ClaveHash.generar("Clave#2026")
            dao.insertar(Usuario(nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = claveHash))
        }
        viewModel = LoginViewModel(AuthRepositorio(dao, dispatcher), this::texto)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun escribir(identificador: String, clave: String) {
        viewModel.onIdentificadorChange(identificador)
        viewModel.onClaveChange(clave)
    }

    @Test
    fun conLosCamposVaciosMuestraUnErrorEnCadaUno() {
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertNotNull(estado.errorIdentificador)
        assertNotNull(estado.errorClave)
        assertFalse(estado.cargando)
        assertFalse(estado.sesionIniciada)
    }

    @Test
    fun unIdentificadorSoloConEspaciosCuentaComoVacio() {
        escribir("   ", "Clave#2026")
        viewModel.iniciarSesion()
        assertNotNull(viewModel.estado.value.errorIdentificador)
        assertNull(viewModel.estado.value.errorClave)
    }

    @Test
    fun alEscribirEnUnCampoDesapareceSuError() {
        viewModel.iniciarSesion()
        viewModel.onIdentificadorChange("ana")
        assertNull(viewModel.estado.value.errorIdentificador)
        assertNotNull(viewModel.estado.value.errorClave)
        viewModel.onClaveChange("x")
        assertNull(viewModel.estado.value.errorClave)
    }

    @Test
    fun conCredencialesCorrectasInicia() {
        escribir("ana_23", "Clave#2026")
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertTrue(estado.sesionIniciada)
        assertEquals(1L, estado.usuarioId)
        assertFalse(estado.cargando)
        assertNull(estado.errorGeneral)
        assertEquals("", estado.clave)
    }

    @Test
    fun conClaveIncorrectaMuestraUnErrorYNoInicia() {
        escribir("ana_23", "mal")
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertFalse(estado.sesionIniciada)
        assertFalse(estado.cargando)
        assertNotNull(estado.errorGeneral)
    }

    @Test
    fun conUnUsuarioInexistenteMuestraElMismoErrorQueConClaveIncorrecta() {
        escribir("ana_23", "mal")
        viewModel.iniciarSesion()
        val conClaveMal = viewModel.estado.value.errorGeneral
        escribir("nadie", "Clave#2026")
        viewModel.iniciarSesion()
        assertEquals(conClaveMal, viewModel.estado.value.errorGeneral)
    }

    @Test
    fun unFalloInesperadoMuestraUnMensajeGenerico() {
        dao.fallo = IllegalStateException("fallo de prueba")
        escribir("ana_23", "Clave#2026")
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertNotNull(estado.errorGeneral)
        assertFalse(estado.cargando)
        assertFalse(estado.sesionIniciada)
    }
}
