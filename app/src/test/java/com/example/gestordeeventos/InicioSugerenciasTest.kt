package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.ui.inicio.seleccionarProximo
import com.example.gestordeeventos.ui.inicio.seleccionarSugerencias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Qué eventos salen en Inicio: las sugerencias para unirse y el próximo evento del usuario. */
class InicioSugerenciasTest {

    private val ahora = 1_000_000_000L
    private val yo = 1L
    private val hora = 3_600_000L

    private fun conCupo(
        id: Long,
        nombre: String = "Evento $id",
        deporte: Deporte = Deporte.FUTBOL,
        publico: Boolean = true,
        max: Int = 10,
        apuntados: Int = 2,
        yoApuntado: Boolean = false,
        creador: Long = 2L,
        fecha: Long = ahora + hora,
        estado: EstadoEvento = EstadoEvento.PUBLICADO,
        direccion: String = "Parque Norte",
    ) = EventoConCupo(
        evento = Evento(
            id = id, nombre = nombre, deporte = deporte, publico = publico, maxParticipantes = max,
            direccion = direccion, fechaHora = fecha, creadorId = creador, estado = estado,
        ),
        apuntados = apuntados,
        yoApuntado = yoApuntado,
    )

    private fun ids(lista: List<com.example.gestordeeventos.ui.inicio.EventoResumen>) = lista.map { it.id }

    // ---------- Sugerencias ----------

    @Test
    fun muestraUnEventoPublicoFuturoDeOtroUsuario() {
        val resultado = seleccionarSugerencias(listOf(conCupo(1)), yo, ahora)
        assertEquals(listOf("1"), ids(resultado))
    }

    @Test
    fun noMuestraMisPropiosEventos() {
        assertTrue(seleccionarSugerencias(listOf(conCupo(1, creador = yo)), yo, ahora).isEmpty())
    }

    @Test
    fun noMuestraEventosALosQueYaEstoyApuntado() {
        assertTrue(seleccionarSugerencias(listOf(conCupo(1, yoApuntado = true)), yo, ahora).isEmpty())
    }

    @Test
    fun noMuestraEventosCompletos() {
        assertTrue(seleccionarSugerencias(listOf(conCupo(1, max = 4, apuntados = 4)), yo, ahora).isEmpty())
    }

    @Test
    fun unEventoSinLimiteNuncaEstaCompleto() {
        val resultado = seleccionarSugerencias(listOf(conCupo(1, max = 0, apuntados = 50)), yo, ahora)
        assertEquals(1, resultado.size)
        assertEquals("Sin límite de plazas", resultado[0].aviso)
    }

    @Test
    fun noMuestraEventosPasadosNiPrivadosNiCancelados() {
        val lista = listOf(
            conCupo(1, fecha = ahora - hora),
            conCupo(2, publico = false),
            conCupo(3, estado = EstadoEvento.CANCELADO),
        )
        assertTrue(seleccionarSugerencias(lista, yo, ahora).isEmpty())
    }

    @Test
    fun ordenaPorFechaYLimitaElNumero() {
        val lista = listOf(
            conCupo(1, fecha = ahora + 3 * hora),
            conCupo(2, fecha = ahora + 1 * hora),
            conCupo(3, fecha = ahora + 2 * hora),
        )
        assertEquals(listOf("2", "3", "1"), ids(seleccionarSugerencias(lista, yo, ahora)))
        assertEquals(listOf("2", "3"), ids(seleccionarSugerencias(lista, yo, ahora, maximo = 2)))
    }

    @Test
    fun elAvisoDependeDeLasPlazasLibres() {
        val varias = seleccionarSugerencias(listOf(conCupo(1, max = 14, apuntados = 8)), yo, ahora).single()
        assertEquals("6 plazas libres", varias.aviso)
        assertFalse(varias.urgente)

        val ultima = seleccionarSugerencias(listOf(conCupo(2, max = 4, apuntados = 3)), yo, ahora).single()
        assertEquals("¡Última plaza disponible!", ultima.aviso)
        assertTrue(ultima.urgente)
    }

    @Test
    fun elResumenLlevaElDeporteYLosInscritos() {
        val resumen = seleccionarSugerencias(
            listOf(conCupo(7, nombre = "Pádel dobles", deporte = Deporte.PADEL, max = 4, apuntados = 3)), yo, ahora,
        ).single()
        assertEquals("7", resumen.id)
        assertEquals("Pádel dobles", resumen.titulo)
        assertEquals("PADEL", resumen.deporte)
        assertEquals(3, resumen.inscritos)
        assertEquals(4, resumen.plazas)
        assertTrue(resumen.detalle.endsWith("3/4 inscritos"))
    }

    // ---------- Próximo evento ----------

    @Test
    fun sinEventosNoHayProximo() {
        assertNull(seleccionarProximo(emptyList(), ahora))
    }

    @Test
    fun elProximoEsElMasCercanoEnElTiempo() {
        val lista = listOf(
            conCupo(1, nombre = "Lejano", fecha = ahora + 5 * hora),
            conCupo(2, nombre = "Cercano", fecha = ahora + 1 * hora, direccion = "Polideportivo"),
        )
        val proximo = seleccionarProximo(lista, ahora)
        assertNotNull(proximo)
        assertEquals("Cercano", proximo!!.titulo)
        assertEquals("Polideportivo", proximo.lugar)
    }

    @Test
    fun ignoraLosEventosPasadosYCancelados() {
        val lista = listOf(
            conCupo(1, fecha = ahora - hora),
            conCupo(2, estado = EstadoEvento.CANCELADO),
        )
        assertNull(seleccionarProximo(lista, ahora))
    }

    @Test
    fun sinDireccionPonePlaceholder() {
        val proximo = seleccionarProximo(listOf(conCupo(1, direccion = "")), ahora)
        assertEquals("Sin ubicación", proximo!!.lugar)
    }
}
