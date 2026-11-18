# Gestión de eventos: salir y eliminar

## Salirse de un evento (Issue 67)

`EventoRepositorio.salirDeEvento(eventoId, usuarioId)` hace que un usuario deje un evento al que se había unido.
Devuelve un `ResultadoEvento(ok, mensaje)` listo para enseñar.

| Caso | Resultado |
|---|---|
| Estaba apuntado | `ok`: se borra su participación y se libera su plaza. |
| Es el creador | No: no puede salirse de su propio evento (puede eliminarlo, Issue 68). |
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

## Pruebas

- `SalirDeEventoTest` (con emulador): salir, no afectar a otros participantes, liberar plaza, volver a apuntarse en público y no en privado, creador, no apuntado y evento inexistente.
- `DialogosEventoTest` (con emulador): textos y botones del cuadro de confirmación.
