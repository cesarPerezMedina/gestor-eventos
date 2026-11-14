package com.example.gestordeeventos

import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.ui.auth.RegistroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RegistroViewModelTest {

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

    private fun rellenar(
        nombre: String = "ana_23",
        correo: String = "ana@correo.com",
        clave: String = "Clave#2026",
        confirmacion: String = clave
    ) {
        viewModel.onNombreUsuarioChange(nombre)
        viewModel.onCorreoChange(correo)
        viewModel.onClaveChange(clave)
        viewModel.onConfirmacionChange(confirmacion)
    }

    @Test
    fun conTodoVacioMuestraUnErrorEnCadaCampoYNoGuarda() {
        viewModel.registrar()
        val estado = viewModel.estado.value
        assertNotNull(estado.errorNombreUsuario)
        assertNotNull(estado.errorCorreo)
        assertNotNull(estado.errorClave)
        assertNotNull(estado.errorConfirmacion)
        assertFalse(estado.registroCompletado)
        assertEquals(0, dao.guardados.size)
    }

    @Test
    fun muestraSoloElErrorDelCampoIncorrecto() {
        rellenar(correo = "esto-no-es-un-correo")
        viewModel.registrar()
        val estado = viewModel.estado.value
        assertEquals("El formato del correo no es válido", estado.errorCorreo)
        assertNull(estado.errorNombreUsuario)
        assertNull(estado.errorClave)
        assertNull(estado.errorConfirmacion)
        assertEquals(0, dao.guardados.size)
    }

    @Test
    fun avisaSiLasContrasenasNoCoinciden() {
        rellenar(confirmacion = "Otra#Clave2026")
        viewModel.registrar()
        assertEquals("Las contraseñas no coinciden", viewModel.estado.value.errorConfirmacion)
        assertEquals(0, dao.guardados.size)
    }

    @Test
    fun alEscribirEnUnCampoDesapareceSuError() {
        viewModel.registrar()
        viewModel.onCorreoChange("a")
        assertNull(viewModel.estado.value.errorCorreo)
        assertNotNull(viewModel.estado.value.errorNombreUsuario)
    }

    @Test
    fun conDatosCorrectosRegistraYLimpiaLasContrasenas() {
        rellenar()
        viewModel.registrar()
        val estado = viewModel.estado.value
        assertTrue(estado.registroCompletado)
        assertFalse(estado.cargando)
        assertEquals("", estado.clave)
        assertEquals("", estado.confirmacion)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun conUnCorreoYaRegistradoMuestraElErrorEnElCampoCorreo() {
        rellenar()
        viewModel.registrar()
        rellenar(nombre = "otra_ana")
        viewModel.registrar()
        val estado = viewModel.estado.value
        assertNotNull(estado.errorCorreo)
        assertNull(estado.errorNombreUsuario)
        assertFalse(estado.cargando)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun conUnNombreYaRegistradoMuestraElErrorEnElCampoNombre() {
        rellenar()
        viewModel.registrar()
        rellenar(correo = "otra@correo.com")
        viewModel.registrar()
        val estado = viewModel.estado.value
        assertNotNull(estado.errorNombreUsuario)
        assertNull(estado.errorCorreo)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun unFalloInesperadoMuestraUnMensajeGenerico() {
        dao.fallo = IllegalStateException("fallo de prueba")
        rellenar()
        viewModel.registrar()
        val estado = viewModel.estado.value
        assertNotNull(estado.errorGeneral)
        assertFalse(estado.cargando)
        assertFalse(estado.registroCompletado)
    }

    @Test
    fun elAtajoConValoresCopiaLosCamposYRegistra() {
        viewModel.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
        assertTrue(viewModel.estado.value.registroCompletado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun alCorregirUnCorreoYaRegistradoYReenviarDesapareceElError() {
        viewModel.registrar("ana_23", "ana@correo.com", "Clave#2026", "Clave#2026")
        viewModel.registrar("otra_ana", "ana@correo.com", "Clave#2026", "Clave#2026")
        assertNotNull(viewModel.estado.value.errorCorreo)
        viewModel.registrar("otra_ana", "otra@correo.com", "Clave#2026", "Clave#2026")
        val estado = viewModel.estado.value
        assertNull(estado.errorCorreo)
        assertTrue(estado.registroCompletado)
        assertEquals(2, dao.guardados.size)
    }
}
