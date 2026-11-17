# Invitaciones a eventos

## Invitaciones pendientes (Issue 72)

`InvitacionRepositorio.observarPendientes(usuarioId)` devuelve las invitaciones que ha recibido un usuario y que
todavía no ha aceptado ni rechazado. Cada elemento es un `InvitacionPendiente` con lo que necesita la pantalla
(Issue 71): nombre y deporte del evento, fecha, y nombre de quien invita.

- Solo salen las que están en estado `PENDIENTE` y son **para** ese usuario.
- Orden: primero los eventos más próximos en el tiempo.
- Es un `Flow`: la lista se actualiza sola cuando llega, se acepta o se rechaza una invitación (Issue 75).
- `observarNumeroPendientes(usuarioId)` devuelve solo el número (por ejemplo, para un globo en el icono).

### Cómo usarlo desde un ViewModel

```kotlin
val repositorio = InvitacionRepositorio(AppDatabase.obtener(aplicacion).invitacionDao())

val pendientes: StateFlow<List<InvitacionPendiente>> = repositorio
    .observarPendientes(usuarioId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

El `usuarioId` sale de `SesionRepositorio.usuarioActual()?.id`.

## Rechazar una invitación (Issue 74)

`InvitacionRepositorio.rechazar(invitacionId, usuarioId)` marca la invitación como `RECHAZADA`. No se borra:
queda guardada pero deja de salir en las pendientes.

Devuelve un `ResultadoRespuestaInvitacion`; la pantalla decide qué texto enseñar:

| Resultado | Cuándo |
|---|---|
| `OK` | Se ha rechazado. |
| `NO_EXISTE` | La invitación ya no existe (por ejemplo, se borró el evento). |
| `NO_ES_PARA_TI` | La recibió otro usuario: nadie puede responder por los demás. |
| `YA_RESPONDIDA` | Ya estaba aceptada o rechazada; no se puede cambiar. |

El cambio se hace en una sola sentencia SQL (`responderSiPendiente`), así que dos toques seguidos en el botón no
pueden responder dos veces. La Issue 73 (aceptar) usa esa misma sentencia con `EstadoInvitacion.ACEPTADA`.

### Ejemplo desde un ViewModel

```kotlin
fun rechazar(invitacionId: Long) {
    viewModelScope.launch {
        when (repositorio.rechazar(invitacionId, usuarioId)) {
            ResultadoRespuestaInvitacion.OK -> Unit // la lista se actualiza sola
            else -> _mensaje.value = texto(R.string.invitacion_error_respuesta)
        }
    }
}
```

## Pantalla de invitaciones (Issue 71)

`ui/invitaciones/InvitacionesScreen.kt` es la pestaña donde el usuario ve sus invitaciones pendientes. Se llega desde
la campana de notificaciones de Inicio (ruta `Ruta.Invitaciones`).

- **Cada invitación** es una tarjeta con el icono del deporte, el nombre del evento, deporte y fecha, quién invita y
  los botones **Aceptar** y **Rechazar**.
- **Contador** arriba: "1 invitación pendiente" / "N invitaciones pendientes".
- **Sin invitaciones** enseña un mensaje en vez de una lista vacía.
- **Sin estado propio:** recibe la lista (`List<InvitacionPendiente>`) y avisa con `onAceptarClick`, `onRechazarClick`
  y `onBackClick`. De momento enseña datos de ejemplo; la conexión con la base de datos llega en la Issue 75.
- **Accesibilidad:** los botones tienen una descripción con el nombre del evento ("Aceptar la invitación a Fútbol 7"),
  el título es un encabezado y los tamaños de texto siguen el ajuste de la Issue 79. Zonas táctiles de 48 dp.
- **Idiomas:** textos en `strings_invitaciones.xml` (español e inglés).

## Aceptar una invitación (Issue 73)

`InvitacionRepositorio.aceptar(invitacionId, usuarioId)` acepta la invitación y **apunta al usuario al evento**
(público o privado). Todo ocurre dentro de una transacción de Room: o se guarda todo o nada.

1. Comprueba que la invitación existe, es para ese usuario y sigue pendiente.
2. Comprueba que el evento existe y que hay plaza (`maxParticipantes = 0` significa sin límite).
3. Marca la invitación como `ACEPTADA` y añade la participación. Si ya estaba apuntado, no se duplica.

Devuelve el mismo `ResultadoRespuestaInvitacion` que `rechazar`, con un caso más:

| Resultado | Cuándo |
|---|---|
| `OK` | Aceptada y apuntado al evento. |
| `NO_EXISTE` | La invitación o el evento ya no existen. |
| `NO_ES_PARA_TI` | La recibió otro usuario. |
| `YA_RESPONDIDA` | Ya estaba aceptada o rechazada. |
| `EVENTO_COMPLETO` | No quedan plazas. No cambia nada: la invitación sigue pendiente y se puede rechazar. |

`InvitacionRepositorio.crear(context)` crea el repositorio con la transacción real. En los tests se usa
`InvitacionRepositorio(dao)`, que simplemente ejecuta el bloque.

## Pruebas

- `InvitacionRepositorioTest` (sin emulador): filtro por usuario y estado, orden, actualización del Flow y todos los casos de `rechazar`.
- `InvitacionDaoTest` (con emulador): comprueba la consulta SQL real sobre una base de datos en memoria.
- `InvitacionAceptarTest` (sin emulador): aceptar con plaza, evento completo, sin límite, invitación ajena, inexistente, repetida, rechazada y ya apuntado.
- `InvitacionesScreenTest` (con emulador): lista, contador, mensaje vacío y avisos de Aceptar, Rechazar y Volver.
