# Campos de participantes (Issue 44)

`CamposParticipantes` (en `ui/componentes/`) son los dos campos para definir cuántas personas pueden apuntarse a
un evento: **mínimo** y **máximo**, uno al lado del otro. Son una de las piezas del formulario de crear evento.

- **Solo números:** el teclado es numérico y los campos descartan cualquier otro carácter, también al pegar.
  Caben hasta 3 cifras.
- **Reglas** (en `util/ValidadorParticipantes.kt`, Kotlin puro y con tests unitarios):
  - Los dos son obligatorios.
  - Van de 1 a 999. Los límites son dos constantes (`MINIMO_PERMITIDO` y `MAXIMO_PERMITIDO`), fáciles de cambiar.
  - El máximo no puede ser menor que el mínimo. Pueden ser iguales.
- **Errores:** un número mal puesto se avisa al momento. El aviso de "campo obligatorio" solo sale si se pone
  `mostrarErrores = true` (por ejemplo, al pulsar "Crear" con el formulario a medias).
- **Sin estado:** quien lo usa guarda el texto de cada campo. Los textos de error están en español e inglés
  (`strings_participantes.xml`).

```kotlin
var estado by remember { mutableStateOf(ParticipantesUiState()) }
CamposParticipantes(
    estado = estado,
    onMinimoChange = { estado = estado.copy(minimo = it) },
    onMaximoChange = { estado = estado.copy(maximo = it) }
)

// Al guardar:
if (ValidadorParticipantes.sonValidos(estado.minimo, estado.maximo)) {
    // estado.minimo.toInt() y estado.maximo.toInt() ya no pueden fallar
} else {
    estado = estado.copy(mostrarErrores = true)
}
```

Los números se guardan en `Evento.minParticipantes` y `Evento.maxParticipantes`.

Pruebas: `ValidadorParticipantesTest` (sin emulador) y `CamposParticipantesTest` (necesita emulador o móvil). Las
vistas previas están en `CamposParticipantes.kt`.
