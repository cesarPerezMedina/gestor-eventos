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
pueden responder dos veces. La **Issue 73 (aceptar)** puede usar esa misma sentencia con `EstadoInvitacion.ACEPTADA`.

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

## Pruebas

- `InvitacionRepositorioTest` (sin emulador): filtro por usuario y estado, orden, actualización del Flow y todos los casos de `rechazar`.
- `InvitacionDaoTest` (con emulador): comprueba la consulta SQL real sobre una base de datos en memoria.
