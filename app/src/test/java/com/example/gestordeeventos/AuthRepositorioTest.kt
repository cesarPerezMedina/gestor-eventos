package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoLogin
import com.example.gestordeeventos.util.ClaveHash
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositorioTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepositorio(dao, UnconfinedTestDispatcher())

    @Before
    fun crearUsuarioDePrueba() {
        runBlocking {
            val claveHash = ClaveHash.generar("Clave#2026")
            dao.insertar(Usuario(nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = claveHash))
        }
    }

    private fun assertEntra(resultado: ResultadoLogin) {
        assertTrue("Se esperaba Exito y fue $resultado", resultado is ResultadoLogin.Exito)
        assertEquals("ana_23", (resultado as ResultadoLogin.Exito).usuario.nombreUsuario)
    }

    @Test
    fun iniciaSesionConElCorreo() = runTest {
        assertEntra(repositorio.iniciarSesion("ana@correo.com", "Clave#2026"))
    }

    @Test
    fun iniciaSesionConElNombreDeUsuario() = runTest {
        assertEntra(repositorio.iniciarSesion("ana_23", "Clave#2026"))
    }

    @Test
    fun noDistingueMayusculasEnElCorreoNiEnElNombre() = runTest {
        assertEntra(repositorio.iniciarSesion("ANA@Correo.COM", "Clave#2026"))
        assertEntra(repositorio.iniciarSesion("Ana_23", "Clave#2026"))
    }

    @Test
    fun ignoraLosEspaciosAlrededorDelIdentificador() = runTest {
        assertEntra(repositorio.iniciarSesion("  ana_23  ", "Clave#2026"))
    }

    @Test
    fun rechazaUnaClaveIncorrecta() = runTest {
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("ana_23", "otraClave"))
    }

    @Test
    fun laClaveSiDistingueMayusculas() = runTest {
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("ana_23", "clave#2026"))
    }

    @Test
    fun rechazaUnUsuarioQueNoExiste() = runTest {
        assertEquals(ResultadoLogin.CredencialesIncorrectas, repositorio.iniciarSesion("nadie", "Clave#2026"))
    }
}
