package com.example.gestordeeventos

import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import com.example.gestordeeventos.ui.buscar.BuscarViewModel
import com.example.gestordeeventos.ui.buscar.ModoBusqueda
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Los filtros de "Buscar eventos" tal como los usa la pantalla (Issues 56, 57 y 60): solo públicos,
 * "Cerca de mí" con radio, "Por nombre" y chips de deporte. Sin base de datos ni emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val recolector = CoroutineScope(dispatcher)

    private val centro = Ubicacion(latitud = 40.4168, longitud = -3.7038)

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun limpiar() {
        recolector.cancel()
        Dispatchers.resetMain()
    }

    /** Evento a [km] kilómetros al norte del centro. */
    private fun evento(
        id: Long,
        km: Double,
        publico: Boolean = true,
        deporte: Deporte = Deporte.FUTBOL,
        fecha: Long = 1_000L * id,
    ) = Evento(
        id = id,
        nombre = "Evento $id",
        deporte = deporte,
        publico = publico,
        latitud = centro.latitud + km / 111.195,
        longitud = centro.longitud,
        fechaHora = fecha,
        creadorId = 1L,
    )

    private fun crearViewModel(dao: FakeEventoDao): BuscarViewModel {
        val repositorio = EventoRepositorio(dao, FakeInvitacionDao(emptyList(), emptyList()))
        val viewModel = BuscarViewModel(repositorio) { 1L }
        // El estado solo se calcula mientras alguien lo recoge, como hace la pantalla
        recolector.launch { viewModel.estado.collect { } }
        return viewModel
    }

    /** Espera (poco) a que la lista tenga estos ids y los devuelve; si no llega, devuelve los que haya. */
    private fun idsEsperando(viewModel: BuscarViewModel, esperados: List<String>): List<String> = runBlocking {
        try {
            withTimeout(2_000) {
                viewModel.estado.first { estado -> estado.eventos.map { it.id } == esperados }
            }
        } catch (e: TimeoutCancellationException) {
            // Se compara abajo con lo que haya, para que el fallo enseñe la lista real
        }
        viewModel.estado.value.eventos.map { it.id }
    }

    private fun assertIds(viewModel: BuscarViewModel, vararg esperados: String) {
        assertEquals(esperados.toList(), idsEsperando(viewModel, esperados.toList()))
    }

    // --- Sin ubicación ---

    @Test
    fun sinUbicacionMuestraTodosLosPublicosOrdenadosPorFecha() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.0, fecha = 3_000), evento(2, km = 50.0, fecha = 1_000), evento(3, km = 9.0, fecha = 2_000)))
        val viewModel = crearViewModel(dao)

        assertIds(viewModel, "2", "3", "1")
        assertEquals(listOf(null, null, null), viewModel.estado.value.eventos.map { it.distanciaKm })
    }

    // --- Cerca de mí ---

    @Test
    fun conUbicacionSoloSalenLosEventosDentroDelRadio() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.0), evento(2, km = 3.0), evento(3, km = 10.0)))
        val viewModel = crearViewModel(dao)

        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(2)
        assertIds(viewModel, "1")

        viewModel.cambiarRadio(5)
        assertIds(viewModel, "1", "2")

        viewModel.cambiarRadio(25)
        assertIds(viewModel, "1", "2", "3")
    }

    @Test
    fun enCercaDeMiSalenDelMasCercanoAlMasLejano() {
        val dao = FakeEventoDao(listOf(evento(1, km = 8.0, fecha = 1_000), evento(2, km = 1.0, fecha = 2_000), evento(3, km = 4.0, fecha = 3_000)))
        val viewModel = crearViewModel(dao)

        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(10)

        assertIds(viewModel, "2", "3", "1")
    }

    @Test
    fun enCercaDeMiCadaEventoLlevaSuDistancia() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.5)))
        val viewModel = crearViewModel(dao)

        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(5)
        assertIds(viewModel, "1")

        val distancia = viewModel.estado.value.eventos.single().distanciaKm
        assertNotNull(distancia)
        assertEquals(1.5, distancia!!, 0.01)
    }

    @Test
    fun alQuitarLaUbicacionVuelvenTodosLosPublicos() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.0), evento(2, km = 50.0)))
        val viewModel = crearViewModel(dao)
        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(2)
        assertIds(viewModel, "1")

        viewModel.actualizarUbicacion(null)

        assertIds(viewModel, "1", "2")
    }

    // --- Por nombre ---

    @Test
    fun porNombreNoUsaLaUbicacionNiElRadio() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.0), evento(2, km = 50.0), evento(3, km = 300.0)))
        val viewModel = crearViewModel(dao)
        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(2)
        assertIds(viewModel, "1")

        viewModel.cambiarModo(ModoBusqueda.POR_NOMBRE)

        assertIds(viewModel, "1", "2", "3")
        assertNull(viewModel.estado.value.eventos.first().distanciaKm)
    }

    @Test
    fun alVolverACercaDeMiSeAplicaOtraVezElRadio() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.0), evento(2, km = 50.0)))
        val viewModel = crearViewModel(dao)
        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(2)
        viewModel.cambiarModo(ModoBusqueda.POR_NOMBRE)
        assertIds(viewModel, "1", "2")

        viewModel.cambiarModo(ModoBusqueda.CERCA_DE_MI)

        assertIds(viewModel, "1")
    }

    // --- Solo públicos ---

    @Test
    fun losPrivadosNoSalenAunqueLaConsultaLosDevuelva() {
        // El DAO falso no filtra: el ViewModel tiene que descartarlos por su cuenta
        val dao = FakeEventoDao(
            listOf(evento(1, km = 1.0), evento(2, km = 1.0, publico = false)),
            filtrarPublicos = false,
        )
        val viewModel = crearViewModel(dao)

        assertIds(viewModel, "1")

        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(5)

        assertIds(viewModel, "1")
    }

    // --- Actualización en vivo ---

    @Test
    fun siLlegaUnEventoNuevoCercaLaListaSeActualizaSola() {
        val dao = FakeEventoDao(listOf(evento(1, km = 1.0)))
        val viewModel = crearViewModel(dao)
        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(5)
        assertIds(viewModel, "1")

        dao.agregar(evento(9, km = 2.0))
        dao.agregar(evento(10, km = 80.0))

        assertIds(viewModel, "1", "9")
    }

    // --- Chips de deporte ---

    @Test
    fun losChipsCuentanLosEventosDeCadaDeporte() {
        val dao = FakeEventoDao(
            listOf(
                evento(1, km = 1.0, deporte = Deporte.FUTBOL),
                evento(2, km = 1.0, deporte = Deporte.FUTBOL),
                evento(3, km = 1.0, deporte = Deporte.TENIS),
            )
        )
        val viewModel = crearViewModel(dao)
        assertIds(viewModel, "1", "2", "3")

        val cantidades = viewModel.estado.value.filtrosDeporte.associate { it.id to it.cantidad }

        assertEquals(mapOf("todos" to 3, "FUTBOL" to 2, "TENIS" to 1), cantidades)
    }

    @Test
    fun losChipsSoloCuentanLosEventosCercanos() {
        val dao = FakeEventoDao(
            listOf(
                evento(1, km = 1.0, deporte = Deporte.FUTBOL),
                evento(2, km = 50.0, deporte = Deporte.TENIS),
            )
        )
        val viewModel = crearViewModel(dao)
        viewModel.actualizarUbicacion(centro)
        viewModel.cambiarRadio(2)
        assertIds(viewModel, "1")

        val cantidades = viewModel.estado.value.filtrosDeporte.associate { it.id to it.cantidad }

        assertEquals(mapOf("todos" to 1, "FUTBOL" to 1), cantidades)
    }
}
