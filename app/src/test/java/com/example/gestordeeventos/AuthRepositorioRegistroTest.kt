package com.example.gestordeeventos

import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoLogin
import com.example.gestordeeventos.data.repositorio.ResultadoRegistro
import com.example.gestordeeventos.util.ClaveHash
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositorioRegistroTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepositorio(dao, UnconfinedTestDispatcher())

    @Test
    fun registraUnUsuarioNuevo() = runTest {
        val resultado = repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        assertTrue(resultado is ResultadoRegistro.Exito)
        assertEquals(1L, (resultado as ResultadoRegistro.Exito).usuario.id)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun noGuardaLaContrasenaEnTextoPlano() = runTest {
        repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        val guardado = dao.guardados.single()
        assertFalse(guardado.claveHash.contains("Clave#2026"))
        assertTrue(ClaveHash.verificar("Clave#2026", guardado.claveHash))
    }

    @Test
    fun guardaElCorreoEnMinusculasYSinEspacios() = runTest {
        repositorio.registrar("  ana_23 ", "  ANA@Correo.COM ", "Clave#2026")
        val guardado = dao.guardados.single()
        assertEquals("ana@correo.com", guardado.correo)
        assertEquals("ana_23", guardado.nombreUsuario)
    }

    @Test
    fun rechazaUnCorreoYaRegistradoSinDistinguirMayusculas() = runTest {
        repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        val resultado = repositorio.registrar("otra_ana", "ANA@correo.com", "Clave#2026")
        assertEquals(ResultadoRegistro.CorreoEnUso, resultado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun rechazaUnNombreDeUsuarioYaRegistradoSinDistinguirMayusculas() = runTest {
        repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        val resultado = repositorio.registrar("ANA_23", "otra@correo.com", "Clave#2026")
        assertEquals(ResultadoRegistro.NombreUsuarioEnUso, resultado)
        assertEquals(1, dao.guardados.size)
    }

    @Test
    fun despuesDeRegistrarseSePuedeIniciarSesion() = runTest {
        repositorio.registrar("ana_23", "ana@correo.com", "Clave#2026")
        assertTrue(repositorio.iniciarSesion("ana@correo.com", "Clave#2026") is ResultadoLogin.Exito)
        assertTrue(repositorio.iniciarSesion("ana_23", "Clave#2026") is ResultadoLogin.Exito)
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("ana_23", "otra"))
    }
}
