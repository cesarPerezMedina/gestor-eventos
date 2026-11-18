package com.example.gestordeeventos

import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.ui.auth.RegistroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

/** Casos límite del registro que no cubre `RegistroViewModelTest`: textos concretos, reintentos y doble envío. */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelCasosLimiteTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepositorio(dao, dispatcher)
    private lateinit var viewModel: RegistroViewModel

    /** Texto falso para los recursos: así los tests no dependen del idioma. */
    private fun texto(id: Int) = "texto:$id"

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        viewModel = RegistroViewModel(repositorio, this::texto)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun registrarAna() {
        viewModel.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
    }

    @Test
    fun unaContrasenaDebilSoloMarcaElCampoDeLaContrasena() {
        viewModel.registrar("ana_23", "ana@correo.com", "abc", "abc")
        val estado = viewModel.estado.value
        assertEquals("Debe tener al menos 8 caracteres", estado.errorClave)
        assertNull(estado.errorNombreUsuario)
        assertNull(estado.errorCorreo)
        assertNull(estado.errorConfirmacion)
        assertEquals(0, dao.guardados.size)
    }

    @Test
    fun alCambiarLaContrasenaDesapareceElErrorDeConfirmacion() {
        viewModel.registrar("ana_23", "ana@correo.com", "Clave#2026", "Otra#Clave2026")
        assertEquals("Las contraseñas no coinciden", viewModel.estado.value.errorConfirmacion)
        viewModel.onClaveChange("Otra#Clave2026")
        assertNull(viewModel.estado.value.errorConfirmacion)
    }

    @Test
    fun unCorreoRepetidoUsaSuMensajeYConservaLasContrasenas() {
        registrarAna()
        viewModel.registrar("otra_ana", "ana@correo.com", "Clave#2026", "Clave#2026")
        val estado = viewModel.estado.value
        assertEquals(texto(R.string.registro_error_correo_en_uso), estado.errorCorreo)
        assertEquals("Clave#2026", estado.clave)
        assertEquals("Clave#2026", estado.confirmacion)
        assertFalse(estado.registroCompletado)
    }

    @Test
    fun unNombreRepetidoUsaSuMensaje() {
        registrarAna()
        viewModel.registrar("ana_23", "otra@correo.com", "Clave#2026", "Clave#2026")
        assertEquals(texto(R.string.registro_error_nombre_en_uso), viewModel.estado.value.errorNombreUsuario)
    }

    @Test
    fun unFalloInesperadoUsaElMensajeGenerico() {
        dao.fallo = IllegalStateException("fallo de prueba")
        registrarAna()
        assertEquals(texto(R.string.registro_error_generico), viewModel.estado.value.errorGeneral)
    }

    @Test
    fun trasUnFalloInesperadoSePuedeReintentarSinVolverAEscribirTodo() {
        dao.fallo = IllegalStateException("fallo de prueba")
        registrarAna()
        viewModel.onCorreoChange("ana@correo.com")
        assertNull(viewModel.estado.value.errorGeneral)
        dao.fallo = null
        viewModel.registrar()
        assertTrue(viewModel.estado.value.registroCompletado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun elNombreYElCorreoSeGuardanNormalizadosDeExtremoAExtremo() {
        viewModel.registrar("  ana_23 ", "  ANA@Correo.COM ", "Clave#2026", "Clave#2026")
        val guardado = dao.guardados.single()
        assertEquals("ana_23", guardado.nombreUsuario)
        assertEquals("ana@correo.com", guardado.correo)
    }

    @Test
    fun unSegundoEnvioMientrasSeEstaGuardandoSeIgnora() {
        // Con este dispatcher la corrutina no avanza hasta que se le ordena: el primer envío sigue "cargando".
        val diferido = StandardTestDispatcher()
        Dispatchers.setMain(diferido)
        val vm = RegistroViewModel(AuthRepositorio(dao, diferido), this::texto)

        vm.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
        assertTrue(vm.estado.value.cargando)
        vm.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
        diferido.scheduler.advanceUntilIdle()

        val estado = vm.estado.value
        assertTrue(estado.registroCompletado)
        assertNull(estado.errorCorreo)
        assertEquals(1, dao.guardados.size)
    }
}
