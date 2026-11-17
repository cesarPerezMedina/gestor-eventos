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

## Pruebas

- `InvitacionRepositorioTest` (sin emulador): filtro por usuario y estado, orden y actualización del Flow.
- `InvitacionDaoTest` (con emulador): comprueba la consulta SQL real sobre una base de datos en memoria.
