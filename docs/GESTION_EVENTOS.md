# Gestión de eventos: salir y eliminar

## Salirse de un evento (Issue 67)

`EventoRepositorio.salirDeEvento(eventoId, usuarioId)` hace que un usuario deje un evento al que se había unido.
Devuelve un `ResultadoEvento(ok, mensaje)` listo para enseñar.

| Caso | Resultado |
|---|---|
| Estaba apuntado | `ok`: se borra su participación y se libera su plaza. |
| Es el creador | No: no puede salirse de su propio evento (puede eliminarlo, ver más abajo). |
| No estaba apuntado | No: "No estás apuntado a este evento". |
| El evento ya no existe | No: "El evento ya no existe". |

Qué pasa con la invitación: si había aceptado una invitación a ese evento, pasa a **rechazada**. Eso hace que:

- el evento deje de salirle en "Mis eventos" y en `observarEventosUnidos`;
- en un evento **privado** ya no pueda verlo ni volver a apuntarse sin una invitación nueva;
- en un evento **público** pueda volver a apuntarse cuando quiera.

Las listas se actualizan solas porque son `Flow` de Room (Issue 75).

### Cómo usarlo desde una pantalla

```kotlin
var salir by remember { mutableStateOf(false) }
Button(onClick = { salir = true }) { Text("Salir") }

if (salir) {
    ConfirmarSalirEventoDialog(
        nombreEvento = evento.nombre,
        onConfirmar = { salir = false; viewModel.salir(evento.id) },
        onCancelar = { salir = false },
    )
}

// En el ViewModel
fun salir(eventoId: Long) = viewModelScope.launch {
    val resultado = repositorio.salirDeEvento(eventoId, usuarioId)
    _mensaje.value = resultado.mensaje
}
```

`ConfirmarSalirEventoDialog` está en `ui/componentes/DialogosEvento.kt` (textos en español e inglés).

## Eliminar un evento propio (Issue 68)

`EventoRepositorio.eliminarEvento(eventoId, usuarioId)` borra un evento. Devuelve un `ResultadoEvento(ok, mensaje)`.

| Caso | Resultado |
|---|---|
| Lo pide el creador | `ok`: el evento se borra. |
| Lo pide otro usuario | No: "Solo el creador puede eliminar el evento". El evento sigue igual. |
| El evento ya no existe (o se pulsa dos veces) | No: "El evento ya no existe". |

El borrado es **en cascada**: al eliminar el evento, Room borra también sus participaciones y todas sus
invitaciones, pendientes o aceptadas. Desaparece de Buscar, de "Mis eventos" y de "Invitaciones" de todos los
usuarios. Las listas se actualizan solas (son `Flow`).

La comprobación de que es el creador va dentro de la propia sentencia SQL (`DELETE ... WHERE id = ? AND creadorId = ?`),
así que nadie puede borrar un evento ajeno aunque la pantalla falle.

**No se puede deshacer**, por eso la pantalla debe usar antes `ConfirmarEliminarEventoDialog` (en rojo,
`ui/componentes/DialogosEvento.kt`), igual que el de salir:

```kotlin
if (eliminar) {
    ConfirmarEliminarEventoDialog(
        nombreEvento = evento.nombre,
        onConfirmar = { eliminar = false; viewModel.eliminar(evento.id) },
        onCancelar = { eliminar = false },
    )
}
```

## Pruebas

- `SalirDeEventoTest` (con emulador): salir, no afectar a otros participantes, liberar plaza, volver a apuntarse en público y no en privado, creador, no apuntado y evento inexistente.
- `DialogosEventoTest` (con emulador): textos y botones de los cuadros de confirmación de salir y eliminar.
- `EliminarEventoTest` (con emulador): borrar como creador, borrado en cascada, otros usuarios, evento inexistente, doble borrado y listas actualizadas.
