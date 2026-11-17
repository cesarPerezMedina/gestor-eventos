package com.example.gestordeeventos.ui.crear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FormatoFecha = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag("es-ES"))
private val FormatoHora = DateTimeFormatter.ofPattern("HH:mm")

/** "Sáb, 24 Oct": el día de la semana y el mes abreviados, con la primera letra en mayúscula. */
internal fun textoFecha(fecha: LocalDate): String =
    fecha.format(FormatoFecha).replace(".", "").split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

/** "18:30 - 20:00". */
internal fun textoHoras(inicio: LocalTime, fin: LocalTime): String =
    "${inicio.format(FormatoHora)} - ${fin.format(FormatoHora)}"

/** Fecha y hora de inicio en milisegundos desde 1970, que es como guarda `Evento.fechaHora`. */
fun milisegundosDe(fecha: LocalDate, hora: LocalTime): Long =
    LocalDateTime.of(fecha, hora).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Comprueba cuándo se juega el evento. Devuelve el mensaje de error, o null si es válido:
 * no puede estar en el pasado y la hora de fin tiene que ser posterior a la de inicio.
 * [ahora] se puede cambiar para probarla.
 */
fun errorFechaHoraEvento(
    fecha: LocalDate,
    horaInicio: LocalTime,
    horaFin: LocalTime,
    ahora: LocalDateTime = LocalDateTime.now(),
): String? = when {
    fecha.isBefore(ahora.toLocalDate()) -> "Esa fecha ya pasó"
    LocalDateTime.of(fecha, horaInicio).isBefore(ahora) -> "La hora de inicio ya pasó"
    !horaFin.isAfter(horaInicio) -> "La hora de fin debe ser posterior a la de inicio"
    else -> null
}

/**
 * Selector de fecha y hora del evento (Issue 47): un campo "Fecha" que abre un calendario y un campo "Hora"
 * que pide primero la hora de inicio y después la de fin.
 *
 * Es un componente sin estado: quien lo usa guarda los valores y los cambia en los callbacks.
 * El error (fecha pasada, fin antes que inicio) se enseña debajo en cuanto ocurre.
 */
@Composable
fun SelectorFechaHoraEvento(
    fecha: LocalDate,
    onFechaChange: (LocalDate) -> Unit,
    horaInicio: LocalTime,
    horaFin: LocalTime,
    onHorasChange: (inicio: LocalTime, fin: LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var verCalendario by remember { mutableStateOf(false) }
    var verHoras by remember { mutableStateOf(false) }
    val error = errorFechaHoraEvento(fecha, horaInicio, horaFin)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Etiqueta("Fecha")
                Spacer(Modifier.height(6.dp))
                CampoSelector(Icons.Outlined.CalendarMonth, textoFecha(fecha)) { verCalendario = true }
            }
            Column(modifier = Modifier.weight(1f)) {
                Etiqueta("Hora")
                Spacer(Modifier.height(6.dp))
                CampoSelector(Icons.Outlined.Schedule, textoHoras(horaInicio, horaFin)) { verHoras = true }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = error, color = CreError, fontSize = 11.sp)
        }
    }

    if (verCalendario) {
        DialogoFecha(
            fecha = fecha,
            onConfirmar = {
                onFechaChange(it)
                verCalendario = false
            },
            onCancelar = { verCalendario = false },
        )
    }
    if (verHoras) {
        DialogoHoras(
            inicio = horaInicio,
            fin = horaFin,
            onConfirmar = { inicio, fin ->
                onHorasChange(inicio, fin)
                verHoras = false
            },
            onCancelar = { verHoras = false },
        )
    }
}

/** Solo se pueden elegir días desde hoy en adelante. */
@OptIn(ExperimentalMaterial3Api::class)
private object SoloDesdeHoy : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val dia = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return !dia.isBefore(LocalDate.now())
    }

    override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoFecha(fecha: LocalDate, onConfirmar: (LocalDate) -> Unit, onCancelar: () -> Unit) {
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = SoloDesdeHoy,
    )
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = {
                    val elegida = estado.selectedDateMillis
                    if (elegida != null) {
                        onConfirmar(Instant.ofEpochMilli(elegida).atZone(ZoneOffset.UTC).toLocalDate())
                    } else {
                        onCancelar()
                    }
                },
            ) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    ) {
        DatePicker(state = estado)
    }
}

/** Dos pasos: primero la hora de inicio y después la de fin. */
@Composable
private fun DialogoHoras(
    inicio: LocalTime,
    fin: LocalTime,
    onConfirmar: (inicio: LocalTime, fin: LocalTime) -> Unit,
    onCancelar: () -> Unit,
) {
    var inicioElegido by remember { mutableStateOf<LocalTime?>(null) }
    val elegido = inicioElegido

    if (elegido == null) {
        PasoHora(
            titulo = "HORA DE INICIO",
            inicial = inicio,
            textoConfirmar = "Siguiente",
            onConfirmar = { inicioElegido = it },
            onCancelar = onCancelar,
        )
    } else {
        // Si el fin que había ya no es posterior al nuevo inicio, se propone una hora después (sin pasar de medianoche)
        val finPropuesto = when {
            fin.isAfter(elegido) -> fin
            elegido.hour >= 23 -> LocalTime.of(23, 59)
            else -> elegido.plusHours(1)
        }
        PasoHora(
            titulo = "HORA DE FIN",
            inicial = finPropuesto,
            textoConfirmar = "Aceptar",
            onConfirmar = { onConfirmar(elegido, it) },
            onCancelar = onCancelar,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasoHora(
    titulo: String,
    inicial: LocalTime,
    textoConfirmar: String,
    onConfirmar: (LocalTime) -> Unit,
    onCancelar: () -> Unit,
) {
    val estado = rememberTimePickerState(initialHour = inicial.hour, initialMinute = inicial.minute, is24Hour = true)

    Dialog(
        onDismissRequest = onCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                )
                TimePicker(state = estado)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onCancelar) { Text("Cancelar") }
                    TextButton(onClick = { onConfirmar(LocalTime.of(estado.hour, estado.minute)) }) {
                        Text(textoConfirmar)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, name = "Interactivo")
@Composable
private fun SelectorFechaHoraEventoPreview() {
    GestorDeEventosTheme {
        var fecha by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
        var inicio by remember { mutableStateOf(LocalTime.of(18, 30)) }
        var fin by remember { mutableStateOf(LocalTime.of(20, 0)) }
        Column(Modifier.padding(16.dp)) {
            SelectorFechaHoraEvento(
                fecha = fecha,
                onFechaChange = { fecha = it },
                horaInicio = inicio,
                horaFin = fin,
                onHorasChange = { i, f ->
                    inicio = i
                    fin = f
                },
            )
        }
    }
}
