# Validación de eventos (Issues 49 y 50)

`ValidadorEvento` (`util/ValidadorEvento.kt`) comprueba un evento antes de guardarlo. Es Kotlin puro, sin Android ni
base de datos, así que se prueba sin emulador. Devuelve un `ErroresEvento` con un mensaje por campo (o `null` si el
campo está bien), para que el formulario marque cada campo por separado.

## Reglas

| Campo | Regla | Mensaje |
|---|---|---|
| Nombre | Obligatorio; de 3 a 60 caracteres (sin contar espacios de los extremos) | «El nombre del evento es obligatorio» / «El nombre debe tener entre 3 y 60 caracteres» |
| Descripción | Opcional; hasta 500 caracteres | «La descripción no puede superar los 500 caracteres» |
| Participantes | Ni el mínimo ni el máximo negativos; si hay máximo (> 0), no puede ser menor que el mínimo. `0` significa «sin límite» | «El máximo de jugadores no puede ser menor que el mínimo» |
| Ubicación | Hace falta una dirección o unas coordenadas; si hay coordenadas, latitud entre -90 y 90 y longitud entre -180 y 180 | «Indica dónde será el evento» / «Las coordenadas de la ubicación no son válidas» |
| Fecha y hora | Posterior al momento actual | «La fecha debe ser futura» |

## Cómo usarlo

Validar un evento entero:

```kotlin
val errores = ValidadorEvento.validar(evento)
if (!errores.esValido) {
    // errores.nombre, errores.descripcion, errores.participantes, errores.ubicacion, errores.fecha
    // errores.primerError: el primero, en el orden del formulario
}
```

Las reglas sueltas también están disponibles: `validarNombre`, `validarDescripcion`, `validarParticipantes`,
`validarUbicacion` y `validarFecha`.

### Conectarlo a `EventoRepositorio.crearEvento`

Cuando la rama de la Issue 43 esté en `dev`, `crearEvento` tiene dos comprobaciones sueltas (nombre y aforo). Se
sustituyen por el validador:

```kotlin
suspend fun crearEvento(evento: Evento, invitadosIds: List<Long>): ResultadoEvento {
    val errores = ValidadorEvento.validar(evento, exigirFecha = false) // true cuando esté la Issue 47
    errores.primerError?.let { return ResultadoEvento(false, it) }
    // ... el resto sigue igual (guardar y crear las invitaciones)
}
```

### Por qué `exigirFecha`

Hoy el formulario todavía no pide fecha ni hora (Issue 47) y `CrearEventoViewModel` guarda la hora actual como
provisional. Con la regla de fecha activada, ningún evento se podría crear. Cuando exista el selector de fecha, hay
que quitar `exigirFecha = false` (o ponerlo a `true`).

Lo mismo con la ubicación: la regla exige una dirección o coordenadas, y el formulario ya envía la dirección escrita
o la que sale del mapa.

## Pruebas

| Test | Qué comprueba | Cómo se ejecuta |
|---|---|---|
| `ValidadorEventoTest` | Cada regla por separado (nombre, descripción, participantes, ubicación, fecha) y el evento completo | Local (sin emulador) |

Se ejecutan con el botón verde junto a la clase, o con `./gradlew testDebugUnitTest`.
