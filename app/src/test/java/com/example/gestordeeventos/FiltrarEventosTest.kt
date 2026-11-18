package com.example.gestordeeventos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsSoccer
import com.example.gestordeeventos.ui.buscar.EventoBuscarItem
import com.example.gestordeeventos.ui.buscar.filtrarEventos
import org.junit.Assert.assertEquals
import org.junit.Test

/** Filtros de la pantalla Buscar: texto del buscador, chip de deporte y orden "Por nombre". */
class FiltrarEventosTest {

    private fun evento(id: String, titulo: String, deporte: String, lugar: String = "Parque Norte") = EventoBuscarItem(
        id = id,
        titulo = titulo,
        deporteId = deporte,
        icono = Icons.Outlined.SportsSoccer,
        cuando = "Hoy 20:00",
        lugar = lugar,
        apuntados = 1,
        maximo = 10,
    )

    private val lista = listOf(
        evento("1", "Fútbol 7 en el parque", "FUTBOL", "Polideportivo Central"),
        evento("2", "Pádel dobles", "PADEL", "Club Retiro"),
        evento("3", "Baloncesto 3x3", "BALONCESTO", "Pista exterior"),
        evento("4", "Amistoso de fútbol sala", "FUTBOL", "Pabellón Sur"),
    )

    private fun ids(resultado: List<EventoBuscarItem>) = resultado.map { it.id }

    @Test
    fun sinFiltrosDevuelveTodo() {
        assertEquals(listOf("1", "2", "3", "4"), ids(filtrarEventos(lista, "", "todos")))
    }

    @Test
    fun consultaSoloConEspaciosNoFiltra() {
        assertEquals(4, filtrarEventos(lista, "    ", "todos").size)
    }

    @Test
    fun deporteVacioEquivaleATodos() {
        assertEquals(4, filtrarEventos(lista, "", "").size)
    }

    @Test
    fun elChipDeDeporteSoloDejaEseDeporte() {
        assertEquals(listOf("1", "4"), ids(filtrarEventos(lista, "", "FUTBOL")))
        assertEquals(listOf("2"), ids(filtrarEventos(lista, "", "PADEL")))
    }

    @Test
    fun deporteSinEventosDevuelveVacio() {
        assertEquals(emptyList<String>(), ids(filtrarEventos(lista, "", "TENIS")))
    }

    @Test
    fun ignoraMayusculasYTildes() {
        assertEquals(listOf("2"), ids(filtrarEventos(lista, "PADEL", "todos")))
        assertEquals(listOf("1", "4"), ids(filtrarEventos(lista, "futbol", "todos")))
    }

    @Test
    fun variasPalabrasEnCualquierOrden() {
        assertEquals(listOf("4"), ids(filtrarEventos(lista, "sala futbol", "todos")))
    }

    @Test
    fun tambienBuscaEnElLugar() {
        assertEquals(listOf("2"), ids(filtrarEventos(lista, "retiro", "todos")))
    }

    @Test
    fun textoYDeporteSeCombinan() {
        assertEquals(listOf("4"), ids(filtrarEventos(lista, "sala", "FUTBOL")))
        assertEquals(emptyList<String>(), ids(filtrarEventos(lista, "sala", "PADEL")))
    }

    @Test
    fun sinCoincidenciasDevuelveVacio() {
        assertEquals(emptyList<String>(), ids(filtrarEventos(lista, "rugby", "todos")))
    }

    @Test
    fun porNombreOrdenaAlfabeticamenteSinTildes() {
        // "Amistoso", "Baloncesto", "Fútbol", "Pádel"
        assertEquals(listOf("4", "3", "1", "2"), ids(filtrarEventos(lista, "", "todos", ordenarPorNombre = true)))
    }

    @Test
    fun sinOrdenarConservaElOrdenOriginal() {
        assertEquals(listOf("1", "2", "3", "4"), ids(filtrarEventos(lista, "", "todos", ordenarPorNombre = false)))
    }
}
