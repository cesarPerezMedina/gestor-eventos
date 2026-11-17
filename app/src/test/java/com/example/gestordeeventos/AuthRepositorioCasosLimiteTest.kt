package com.example.gestordeeventos

import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoLogin
import com.example.gestordeeventos.data.repositorio.ResultadoRegistro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Casos límite de login y registro que no cubren los tests del repositorio ya existentes (Issue 18). */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositorioCasosLimiteTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepositorio(dao, UnconfinedTestDispatcher())

    private suspend fun registrarAna() {
        repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
    }

    @Test
    fun conUnIdentificadorVacioNoSeEntra() = runTest {
        registrarAna()
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("", "Clave#2026"))
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("   ", "Clave#2026"))
    }

    @Test
    fun laClaveNoSeRecortaAlIniciarSesion() = runTest {
        registrarAna()
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("ana_23", " Clave#2026"))
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("ana_23", "Clave#2026 "))
    }

    @Test
    fun elUsuarioDelRegistroCoincideConElGuardadoYTieneLosDatosNormalizados() = runTest {
        val resultado = repositorio.registrar("  ana_23 ", "  ANA@Correo.COM ", "Clave#2026")
        assertTrue(resultado is ResultadoRegistro.Exito)
        val usuario = (resultado as ResultadoRegistro.Exito).usuario
        assertEquals("ana_23", usuario.nombreUsuario)
        assertEquals("ana@correo.com", usuario.correo)
        assertEquals(dao.guardados.single(), usuario)
    }

    @Test
    fun siRepiteCorreoYNombreAvisaPrimeroDelCorreo() = runTest {
        registrarAna()
        val resultado = repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        assertEquals(ResultadoRegistro.CorreoEnUso, resultado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun detectaElCorreoRepetidoAunqueLleguePorEspaciosYMayusculas() = runTest {
        registrarAna()
        val resultado = repositorio.registrar("otra_ana", "  ANA@Correo.COM  ", "Clave#2026")
        assertEquals(ResultadoRegistro.CorreoEnUso, resultado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun detectaElNombreRepetidoAunqueLlegueConEspacios() = runTest {
        registrarAna()
        val resultado = repositorio.registrar("  ANA_23  ", "otra@correo.com", "Clave#2026")
        assertEquals(ResultadoRegistro.NombreUsuarioEnUso, resultado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun dosUsuariosConLaMismaContrasenaGuardanHashesDistintos() = runTest {
        repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        repositorio.registrar("luis_7", "luis@correo.com", "Clave#2026")
        assertNotEquals(dao.guardados[0].claveHash, dao.guardados[1].claveHash)
    }

    @Test
    fun unFalloDeLaBaseDeDatosSePropagaAlIniciarSesion() = runTest {
        registrarAna()
        dao.fallo = IllegalStateException("fallo de prueba")
        try {
            repositorio.iniciarSesion("ana_23", "Clave#2026")
            fail("Se esperaba una excepción")
        } catch (e: IllegalStateException) {
            assertEquals("fallo de prueba", e.message)
        }
    }

    @Test
    fun unFalloDeLaBaseDeDatosAlRegistrarNoGuardaNada() = runTest {
        dao.fallo = IllegalStateException("fallo de prueba")
        try {
            repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
            fail("Se esperaba una excepción")
        } catch (e: IllegalStateException) {
            assertEquals("fallo de prueba", e.message)
        }
        assertEquals(0, dao.guardados.size)
    }
}
