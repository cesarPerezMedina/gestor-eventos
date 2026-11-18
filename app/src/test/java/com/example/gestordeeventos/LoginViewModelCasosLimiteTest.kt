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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Casos límite del login que no cubre `LoginViewModelTest`: textos concretos, reintentos y errores que se limpian. */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelCasosLimiteTest {

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

    @Test
    fun losCamposVaciosUsanSusMensajes() {
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertEquals(texto(R.string.login_error_identificador_vacio), estado.errorIdentificador)
        assertEquals(texto(R.string.login_error_clave_vacia), estado.errorClave)
    }

    @Test
    fun unaClaveSoloConEspaciosNoCuentaComoVacia() {
        viewModel.iniciarSesion("ana_23", "   ")
        val estado = viewModel.estado.value
        assertNull(estado.errorClave)
        assertEquals(texto(R.string.login_error_credenciales), estado.errorGeneral)
        assertFalse(estado.sesionIniciada)
    }

    @Test
    fun conCredencialesIncorrectasMuestraElMensajeDeCredenciales() {
        viewModel.iniciarSesion("ana_23", "mal")
        assertEquals(texto(R.string.login_error_credenciales), viewModel.estado.value.errorGeneral)
    }

    @Test
    fun unFalloInesperadoMuestraElMensajeGenerico() {
        dao.fallo = IllegalStateException("fallo de prueba")
        viewModel.iniciarSesion("ana_23", "Clave#2026")
        assertEquals(texto(R.string.login_error_generico), viewModel.estado.value.errorGeneral)
    }

    @Test
    fun alEscribirEnCualquierCampoDesapareceElErrorGeneral() {
        viewModel.iniciarSesion("ana_23", "mal")
        viewModel.onIdentificadorChange("ana_2")
        assertNull(viewModel.estado.value.errorGeneral)

        viewModel.iniciarSesion("ana_23", "mal")
        viewModel.onClaveChange("x")
        assertNull(viewModel.estado.value.errorGeneral)
    }

    @Test
    fun trasUnErrorDeCredencialesSePuedeReintentarConLaClaveCorrecta() {
        viewModel.iniciarSesion("ana_23", "mal")
        viewModel.onClaveChange("Clave#2026")
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertTrue(estado.sesionIniciada)
        assertNull(estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    @Test
    fun trasUnFalloInesperadoSePuedeReintentar() {
        dao.fallo = IllegalStateException("fallo de prueba")
        viewModel.iniciarSesion("ana_23", "Clave#2026")
        dao.fallo = null
        viewModel.iniciarSesion()
        val estado = viewModel.estado.value
        assertTrue(estado.sesionIniciada)
        assertNull(estado.errorGeneral)
    }

    @Test
    fun elIdentificadorAceptaMayusculasYEspaciosAlrededor() {
        viewModel.iniciarSesion("  ANA@Correo.COM ", "Clave#2026")
        assertTrue(viewModel.estado.value.sesionIniciada)
    }
}
