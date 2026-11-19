# Ubicación actual del dispositivo (Issue 25)

## Qué hace

`LocalizacionProvider` (en `data/localizacion/`) obtiene la posición del dispositivo **una sola vez** y devuelve un
`ResultadoUbicacion`. No sigue al usuario, no pide ubicación en segundo plano y no guarda ni escribe en ningún log
las coordenadas. Solo obtiene la posición: calcular distancias o recomendar eventos cercanos son otras Issues.

```
data/localizacion/
  LocalizacionProvider.kt   obtenerUbicacionActual()
  Ubicacion.kt              Ubicacion (latitud, longitud, precisión), ResultadoUbicacion y la regla de precisión
```

## Resultados posibles

| Resultado | Cuándo |
|---|---|
| `Obtenida(ubicacion)` | Se calculó la posición. |
| `SinPermiso` | La app no tiene permiso de ubicación (ver `docs/PERMISOS.md`). |
| `UbicacionDesactivada` | El usuario tiene la ubicación apagada en los ajustes del dispositivo. |
| `NoDisponible` | No hay posición (sin señal) o pasaron 10 segundos sin respuesta. |
| `Fallo(causa)` | Los servicios de ubicación de Google devolvieron un error. |

Si el usuario solo concedió la ubicación aproximada, se pide con precisión equilibrada (gasta menos batería). Si
concedió la precisa, con alta precisión.

## Cómo usarlo desde una pantalla

El permiso se pide con la pieza de la Issue 24, y después se lee la posición:

```kotlin
val contexto = LocalContext.current
val localizacion = remember { LocalizacionProvider(contexto) }
val permiso = rememberSolicitudPermiso(PermisoApp.UBICACION)
var resultado by remember { mutableStateOf<ResultadoUbicacion?>(null) }

LaunchedEffect(permiso.concedido) {
    if (permiso.concedido) resultado = localizacion.obtenerUbicacionActual()
}
```

## Requisitos

- Usa la librería `play-services-location` (añadida en `libs.versions.toml` y `app/build.gradle.kts`), así que el
  dispositivo necesita **Google Play Services**. En el emulador, crea el dispositivo con una imagen que tenga el
  icono de Google Play.
- El manifest ya declara los permisos de ubicación (Issue 24); esta Issue no lo modifica.

## Cómo probarlo

- Test sin emulador: `app/src/test/.../LocalizacionTest.kt` (regla de precisión), con `./gradlew test`.
- A mano, en emulador o móvil (la ubicación real no se puede comprobar con tests locales):
  1. En el emulador, abrir **Extended controls (⋯) → Location** y fijar unas coordenadas.
  2. Sin permiso: debe devolver `SinPermiso`.
  3. Con permiso y la ubicación del dispositivo apagada: debe devolver `UbicacionDesactivada`.
  4. Con permiso y la ubicación encendida: debe devolver `Obtenida` con las coordenadas fijadas.
