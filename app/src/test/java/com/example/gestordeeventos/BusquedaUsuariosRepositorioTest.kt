package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.BusquedaUsuariosRepositorio
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BusquedaUsuariosRepositorioTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = BusquedaUsuariosRepositorio(dao)
    private val miCorreo = "yo@correo.com"

    private suspend fun crear(nombre: String) {
        dao.insertar(Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash"))
    }

    /** Crea ana_23 (id 1), luis_7 (id 2), juana (id 3) y bea (id 4). */
    @Before
    fun crearUsuarios() {
        runBlocking {
            listOf("ana_23", "luis_7", "juana", "bea").forEach { crear(it) }
        }
    }

    private suspend fun nombres(consulta: String, correo: String = miCorreo) =
        repositorio.buscar(consulta, correo).map { it.nombreUsuario }

    @Test
    fun unaConsultaVaciaNoDevuelveNada() = runTest {
        assertTrue(repositorio.buscar("   ", miCorreo).isEmpty())
        assertTrue(repositorio.buscar("@", miCorreo).isEmpty())
    }

    @Test
    fun buscaPorParteDelNombreSinDistinguirMayusculas() = runTest {
        assertEquals(listOf("ana_23", "juana"), nombres("ANA"))
    }

    @Test
    fun ignoraLaArrobaInicial() = runTest {
        assertEquals(listOf("luis_7"), nombres("@luis"))
    }

    @Test
    fun conUnNumeroBuscaPorIdYTambienPorNombre() = runTest {
        // El ID 2 es luis_7 (va primero); "ana_23" también contiene un 2.
        assertEquals(listOf("luis_7", "ana_23"), nombres("2"))
    }

    @Test
    fun noDevuelveAlUsuarioQueBusca() = runTest {
        assertEquals(listOf("juana"), nombres("ana", correo = "ana_23@correo.com"))
        assertTrue(nombres("1", correo = "ana_23@correo.com").isEmpty())
    }

    @Test
    fun elNombreExactoVaPrimero() = runTest {
        crear("ana")
        assertEquals(listOf("ana", "ana_23", "juana"), nombres("ana"))
    }

    @Test
    fun elGuionBajoSeBuscaTalCual() = runTest {
        assertEquals(listOf("ana_23"), nombres("a_"))
    }

    @Test
    fun limitaElNumeroDeResultados() = runTest {
        (1..25).forEach { crear("dep_%02d".format(it)) }
        assertEquals(BusquedaUsuariosRepositorio.LIMITE_RESULTADOS, nombres("dep").size)
    }

    @Test
    fun elResultadoSoloTieneIdYNombreDeUsuario() = runTest {
        val encontrado = repositorio.buscar("luis", miCorreo).single()
        assertEquals(2L, encontrado.id)
        assertEquals("luis_7", encontrado.nombreUsuario)
    }
}
