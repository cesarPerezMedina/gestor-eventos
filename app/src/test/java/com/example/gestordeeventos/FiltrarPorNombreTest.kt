package com.example.gestordeeventos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsSoccer
import com.example.gestordeeventos.ui.buscar.EventoBuscarItem
import com.example.gestordeeventos.ui.buscar.filtrarPorNombre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** Filtro de la búsqueda por nombre (Issues 55 y 60). Se ejecuta sin emulador. */
class FiltrarPorNombreTest {

    private fun item(id: String, titulo: String, lugar: String = "") = EventoBuscarItem(
        id = id,
        titulo = titulo,
        icono = Icons.Outlined.SportsSoccer,
        cuando = "Hoy 20:00",
        lugar = lugar,
        apuntados = 0,
        maximo = 10,
    )

    private val futbol = item("1", "Fútbol 7 en el parque", lugar = "Campo Norte")
    private val padel = item("2", "Pádel mixto", lugar = "Club Sur")
    private val baloncesto = item("3", "Baloncesto 3x3", lugar = "Pista Centro")
    private val nandu = item("4", "Torneo Ñandú de tenis")
    private val lista = listOf(futbol, padel, baloncesto, nandu)

    private fun ids(consulta: String) = filtrarPorNombre(lista, consulta).map { it.id }

    // --- Consulta vacía ---

    @Test
    fun consultaVaciaDevuelveTodo() {
        assertEquals(listOf("1", "2", "3", "4"), ids(""))
    }

    @Test
    fun consultaSoloConEspaciosDevuelveTodo() {
        assertEquals(listOf("1", "2", "3", "4"), ids("    "))
    }

    @Test
    fun consultaVaciaDevuelveLaMismaLista() {
        assertSame(lista, filtrarPorNombre(lista, ""))
    }

    // --- Mayúsculas y tildes ---

    @Test
    fun ignoraMayusculas() {
        assertEquals(listOf("2"), ids("PÁDEL"))
        assertEquals(listOf("3"), ids("baloncesto"))
        assertEquals(listOf("3"), ids("BALONCESTO"))
    }

    @Test
    fun laConsultaSinTildeEncuentraTitulosConTilde() {
        assertEquals(listOf("1"), ids("futbol"))
        assertEquals(listOf("2"), ids("padel"))
    }

    @Test
    fun laConsultaConTildeEncuentraTitulosSinTilde() {
        val sinTilde = item("9", "Futbol sala")
        assertEquals(listOf("9"), filtrarPorNombre(listOf(sinTilde, padel), "FÚTBOL").map { it.id })
    }

    @Test
    fun laEnyeSeTrataComoLaNSinTilde() {
        assertEquals(listOf("4"), ids("nandu"))
        assertEquals(listOf("4"), ids("ñandú"))
    }

    // --- Palabras ---

    @Test
    fun bastaConUnaParteDeLaPalabra() {
        assertEquals(listOf("1"), ids("fut"))
        assertEquals(listOf("3"), ids("balon"))
    }

    @Test
    fun variasPalabrasEnCualquierOrden() {
        assertEquals(listOf("2"), ids("mixto padel"))
        assertEquals(listOf("2"), ids("padel mixto"))
    }

    @Test
    fun hacenFaltaTodasLasPalabras() {
        assertEquals(emptyList<String>(), ids("padel rugby"))
    }

    @Test
    fun losEspaciosSobrantesNoCuentan() {
        assertEquals(listOf("2"), ids("   padel      mixto   "))
    }

    @Test
    fun unaPalabraPuedeEncontrarVariosEventos() {
        val otro = item("5", "Torneo de pádel")
        assertEquals(listOf("2", "5"), filtrarPorNombre(listOf(futbol, padel, otro), "padel").map { it.id })
    }

    // --- Resultado ---

    @Test
    fun sinCoincidenciasDevuelveVacio() {
        assertEquals(emptyList<String>(), ids("rugby"))
    }

    @Test
    fun conLaListaVaciaDevuelveVacio() {
        assertEquals(emptyList<EventoBuscarItem>(), filtrarPorNombre(emptyList(), "padel"))
    }

    @Test
    fun mantieneElOrdenOriginal() {
        val a = item("a", "Partido 1")
        val b = item("b", "Partido 2")
        val c = item("c", "Partido 3")
        assertEquals(listOf("a", "b", "c"), filtrarPorNombre(listOf(a, b, c), "partido").map { it.id })
    }

    @Test
    fun devuelveLosMismosElementos() {
        assertSame(padel, filtrarPorNombre(lista, "padel").single())
    }

    // --- Qué se mira ---

    @Test
    fun soloMiraElTituloNoElLugar() {
        // "Club Sur" es el lugar del evento 2, no está en su título
        assertEquals(emptyList<String>(), ids("club"))
    }
}
