package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.AlmacenSesion
import com.example.gestordeeventos.data.repositorio.SesionRepositorio
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Almacén en memoria para probar sin Android. */
private class AlmacenSesionEnMemoria : AlmacenSesion {
    var id: Long? = null
    override fun leerUsuarioId(): Long? = id
    override fun guardarUsuarioId(id: Long) {
        this.id = id
    }
    override fun borrar() {
        id = null
    }
}

class SesionRepositorioTest {

    private val dao = FakeUsuarioDao()
    private val almacen = AlmacenSesionEnMemoria()
    private val repositorio = SesionRepositorio(almacen, dao)

    private suspend fun crearUsuario(nombre: String = "ana_23"): Long =
        dao.insertar(Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash"))

    @Test
    fun sinSesionGuardadaNoHayUsuario() = runTest {
        assertFalse(repositorio.haySesionGuardada())
        assertNull(repositorio.usuarioActual())
    }

    @Test
    fun despuesDeIniciarDevuelveElUsuarioGuardado() = runTest {
        val id = crearUsuario()
        repositorio.iniciar(id)
        assertTrue(repositorio.haySesionGuardada())
        assertEquals("ana_23", repositorio.usuarioActual()?.nombreUsuario)
    }

    @Test
    fun laSesionSigueAunqueSeCreeOtroRepositorioConElMismoAlmacen() = runTest {
        val id = crearUsuario()
        repositorio.iniciar(id)
        val tras = SesionRepositorio(almacen, dao) // simula cerrar y volver a abrir la app
        assertEquals(id, tras.usuarioActual()?.id)
    }

    @Test
    fun iniciarOtraSesionSustituyeLaAnterior() = runTest {
        val ana = crearUsuario("ana_23")
        val luis = crearUsuario("luis_7")
        repositorio.iniciar(ana)
        repositorio.iniciar(luis)
        assertEquals("luis_7", repositorio.usuarioActual()?.nombreUsuario)
    }

    @Test
    fun cerrarOlvidaLaSesion() = runTest {
        repositorio.iniciar(crearUsuario())
        repositorio.cerrar()
        assertFalse(repositorio.haySesionGuardada())
        assertNull(repositorio.usuarioActual())
    }

    @Test
    fun siElUsuarioYaNoExisteSeBorraLaSesion() = runTest {
        repositorio.iniciar(999L)
        assertNull(repositorio.usuarioActual())
        assertFalse(repositorio.haySesionGuardada())
    }
}
