package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.UsuarioEncontrado
import com.example.gestordeeventos.data.repositorio.AmigosRepositorio
import com.example.gestordeeventos.data.repositorio.escaparParaLike
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Comprueba cómo prepara la búsqueda de usuarios el repositorio, sin base de datos ni emulador. */
class AmigosRepositorioTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = AmigosRepositorio(dao)

    @Test
    fun siElTextoEstaVacioNoConsultaYDevuelveListaVacia() = runTest {
        assertEquals(emptyList<UsuarioEncontrado>(), repositorio.buscarUsuarios("   ", usuarioActualId = 1))
        assertNull(dao.ultimaBusqueda)
    }

    @Test
    fun siSoloEscribeLaArrobaNoConsulta() = runTest {
        repositorio.buscarUsuarios(" @ ", usuarioActualId = 1)
        assertNull(dao.ultimaBusqueda)
    }

    @Test
    fun quitaEspaciosYLaArrobaInicial() = runTest {
        repositorio.buscarUsuarios("  @marta ", usuarioActualId = 1)
        assertEquals("marta", dao.ultimaBusqueda?.patron)
    }

    @Test
    fun unNumeroSeBuscaComoIdYTambienComoTexto() = runTest {
        repositorio.buscarUsuarios("12", usuarioActualId = 1)
        assertEquals(12L, dao.ultimaBusqueda?.id)
        assertEquals("12", dao.ultimaBusqueda?.patron)
    }

    @Test
    fun unNombreNoNumericoNoTieneId() = runTest {
        repositorio.buscarUsuarios("ana_23", usuarioActualId = 1)
        assertNull(dao.ultimaBusqueda?.id)
    }

    @Test
    fun unNumeroDemasiadoGrandeSoloSeBuscaPorNombre() = runTest {
        repositorio.buscarUsuarios("99999999999999999999", usuarioActualId = 1)
        assertNull(dao.ultimaBusqueda?.id)
        assertEquals("99999999999999999999", dao.ultimaBusqueda?.patron)
    }

    @Test
    fun escapaLosComodinesDeLike() = runTest {
        repositorio.buscarUsuarios("a_b%c", usuarioActualId = 1)
        assertEquals("a\\_b\\%c", dao.ultimaBusqueda?.patron)
    }

    @Test
    fun pasaElUsuarioActualYElLimite() = runTest {
        repositorio.buscarUsuarios("marta", usuarioActualId = 7, limite = 5)
        assertEquals(7L, dao.ultimaBusqueda?.usuarioActualId)
        assertEquals(5, dao.ultimaBusqueda?.limite)
    }

    @Test
    fun usaElLimitePorDefecto() = runTest {
        repositorio.buscarUsuarios("marta", usuarioActualId = 1)
        assertEquals(AmigosRepositorio.LIMITE_RESULTADOS, dao.ultimaBusqueda?.limite)
    }

    @Test
    fun devuelveLosUsuariosQueEncuentraElDao() = runTest {
        val encontrados = listOf(UsuarioEncontrado(id = 3, nombreUsuario = "marta_padel", yaEsAmigo = true))
        dao.resultadosBusqueda = encontrados
        assertEquals(encontrados, repositorio.buscarUsuarios("marta", usuarioActualId = 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaUnLimiteQueNoEsPositivo() = runTest {
        repositorio.buscarUsuarios("marta", usuarioActualId = 1, limite = 0)
    }

    @Test
    fun escaparParaLikeEscapaBarraPorcentajeYGuionBajo() {
        assertEquals("a\\\\b\\%c\\_d", escaparParaLike("a\\b%c_d"))
    }

    @Test
    fun escaparParaLikeNoCambiaUnTextoNormal() {
        assertEquals("marta99", escaparParaLike("marta99"))
    }
}
