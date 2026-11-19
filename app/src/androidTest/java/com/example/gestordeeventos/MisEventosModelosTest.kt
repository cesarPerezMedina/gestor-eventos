package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.ui.evento.EtiquetaEvento
import com.example.gestordeeventos.ui.evento.FiltroMisEventos
import com.example.gestordeeventos.ui.evento.MiEventoItem
import com.example.gestordeeventos.ui.evento.categoria
import com.example.gestordeeventos.ui.evento.contar
import com.example.gestordeeventos.ui.evento.filtrarMisEventos
import org.junit.Assert.assertEquals
import org.junit.Test

/** Reglas de la pantalla "Mis eventos" (Issue 63): en qué chip cae cada evento, etiquetas y búsqueda. */
class MisEventosModelosTest {

    private val hora = 3_600_000L
    private val ahora = 1_000 * hora

    private fun evento(
        id: Long = 1,
        nombre: String = "Fútbol 7 en el parque",
        deporte: Deporte = Deporte.FUTBOL,
        fechaHora: Long = ahora + 24 * hora,
        lugar: String = "Parque Central",
        apuntados: Int = 8,
        minimo: Int = 10,
        maximo: Int = 14,
        publico: Boolean = true,
        estado: EstadoEvento = EstadoEvento.PUBLICADO,
    ) = MiEventoItem(id, nombre, deporte, fechaHora, lugar, apuntados, minimo, maximo, publico, estado, esCreador = true)

    private val nombreDeporte: (Deporte) -> String = { if (it == Deporte.PADEL) "Pádel" else it.name.lowercase() }

    @Test
    fun evento_futuro_publicado_esProximo() {
        assertEquals(FiltroMisEventos.PROXIMOS, evento().categoria(ahora))
    }

    @Test
    fun evento_pasado_sigue_en_proximos_hasta_48_horas_y_despues_pasa_al_historial() {
        val hace47h = evento(fechaHora = ahora - 47 * hora)
        val hace49h = evento(fechaHora = ahora - 49 * hora)
        assertEquals(FiltroMisEventos.PROXIMOS, hace47h.categoria(ahora))
        assertEquals(FiltroMisEventos.HISTORIAL, hace49h.categoria(ahora))
    }

    @Test
    fun borrador_y_cancelado_tienen_su_propio_chip_aunque_la_fecha_sea_futura() {
        assertEquals(FiltroMisEventos.BORRADORES, evento(estado = EstadoEvento.BORRADOR).categoria(ahora))
        assertEquals(FiltroMisEventos.CANCELADOS, evento(estado = EstadoEvento.CANCELADO).categoria(ahora))
    }

    @Test
    fun evento_archivado_va_al_historial_aunque_su_fecha_sea_futura() {
        assertEquals(FiltroMisEventos.HISTORIAL, evento(estado = EstadoEvento.ARCHIVADO).categoria(ahora))
    }

    @Test
    fun contar_solo_cuenta_los_del_chip() {
        val lista = listOf(
            evento(id = 1),
            evento(id = 2),
            evento(id = 3, estado = EstadoEvento.BORRADOR),
            evento(id = 4, fechaHora = ahora - 100 * hora),
        )
        assertEquals(2, lista.contar(FiltroMisEventos.PROXIMOS, ahora))
        assertEquals(1, lista.contar(FiltroMisEventos.BORRADORES, ahora))
        assertEquals(1, lista.contar(FiltroMisEventos.HISTORIAL, ahora))
        assertEquals(0, lista.contar(FiltroMisEventos.CANCELADOS, ahora))
    }

    @Test
    fun etiqueta_publico_privado_y_confirmado_cuando_esta_completo() {
        assertEquals(EtiquetaEvento.PUBLICO, evento().etiqueta)
        assertEquals(EtiquetaEvento.PRIVADO, evento(publico = false).etiqueta)
        assertEquals(EtiquetaEvento.CONFIRMADO, evento(apuntados = 6, minimo = 6, maximo = 6).etiqueta)
        assertEquals(EtiquetaEvento.BORRADOR, evento(estado = EstadoEvento.BORRADOR).etiqueta)
        assertEquals(EtiquetaEvento.CANCELADO, evento(estado = EstadoEvento.CANCELADO).etiqueta)
    }

    @Test
    fun cupo_cuantos_faltan_para_jugar_y_plazas_libres() {
        val e = evento(apuntados = 8, minimo = 10, maximo = 14)
        assertEquals(2, e.faltanParaJugar)
        assertEquals(6, e.plazasLibres)
        // Sin límite de plazas ni mínimo: nunca "falta" nadie
        val libre = evento(apuntados = 3, minimo = 0, maximo = 0)
        assertEquals(0, libre.faltanParaJugar)
        assertEquals(0, libre.plazasLibres)
    }

    @Test
    fun buscar_por_deporte_o_lugar_sin_importar_tildes_ni_mayusculas() {
        val padel = evento(id = 2, nombre = "Dobles mixto", deporte = Deporte.PADEL, lugar = "Padel Center pista 4")
        val futbol = evento(id = 1)
        val lista = listOf(futbol, padel)

        val porDeporte = filtrarMisEventos(lista, FiltroMisEventos.PROXIMOS, "PADEL", ahora, nombreDeporte)
        val porLugar = filtrarMisEventos(lista, FiltroMisEventos.PROXIMOS, "parque central", ahora, nombreDeporte)
        val sinResultados = filtrarMisEventos(lista, FiltroMisEventos.PROXIMOS, "baloncesto", ahora, nombreDeporte)

        assertEquals(listOf(2L), porDeporte.map { it.id })
        assertEquals(listOf(1L), porLugar.map { it.id })
        assertEquals(emptyList<Long>(), sinResultados.map { it.id })
    }

    @Test
    fun orden_proximos_del_mas_cercano_e_historial_del_mas_reciente() {
        val lejano = evento(id = 1, fechaHora = ahora + 72 * hora)
        val cercano = evento(id = 2, fechaHora = ahora + 2 * hora)
        val viejo = evento(id = 3, fechaHora = ahora - 200 * hora)
        val reciente = evento(id = 4, fechaHora = ahora - 60 * hora)
        val lista = listOf(lejano, cercano, viejo, reciente)

        assertEquals(listOf(2L, 1L), filtrarMisEventos(lista, FiltroMisEventos.PROXIMOS, "", ahora, nombreDeporte).map { it.id })
        assertEquals(listOf(4L, 3L), filtrarMisEventos(lista, FiltroMisEventos.HISTORIAL, "", ahora, nombreDeporte).map { it.id })
    }
}