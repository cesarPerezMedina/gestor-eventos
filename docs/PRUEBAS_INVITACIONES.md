# Pruebas del flujo de invitaciones (Issue 76)

Qué se prueba del flujo completo: **crear un evento con invitados → recibirlas → aceptar o rechazar → ver el
resultado en las listas**. Hay pruebas en tres niveles; cada una cubre lo que las otras no pueden.

| Nivel | Archivo | Necesita emulador | Qué comprueba |
|---|---|---|---|
| Lógica (unitaria) | `InvitacionRepositorioTest` | No | Pendientes por usuario y estado, orden, `rechazar` (Issues 72 y 74). |
| Lógica (unitaria) | `InvitacionAceptarTest` | No | `aceptar`: apuntarse, plazas, límite, invitación ajena, repetida, rechazada (Issue 73). |
| Lógica (unitaria) | `InvitacionesViewModelTest` | No | La lista se actualiza sola, avisos del resultado, un solo aviso por respuesta (Issue 75). |
| Lógica (unitaria) | `FlujoInvitacionesRepositorioTest` | No | Flujo con varios usuarios: se llena el evento, se libera una plaza, rechazadas que no vuelven. |
| Base de datos | `InvitacionDaoTest` | Sí | La consulta SQL de pendientes sobre una base de datos real en memoria. |
| Integración | `FlujoInvitacionesTest` | Sí | El flujo completo con Room real y la transacción de la app, junto con `EventoRepositorio`. |
| Interfaz | `InvitacionesScreenTest` | Sí | La pantalla: lista, contador, vacío y botones (Issue 71). |

## Casos de integración (`FlujoInvitacionesTest`)

1. Crear un evento con invitados deja una invitación pendiente para cada uno (y ninguna para el creador).
2. Aceptar apunta al usuario; el evento sale en "unidos" y "mis eventos"; la invitación de otro no cambia.
3. Rechazar no apunta; en un evento privado deja de verlo y ya no puede apuntarse.
4. Con el evento completo, aceptar no cambia nada y la invitación sigue pendiente (se puede rechazar).
5. No se puede responder dos veces ni por otro usuario.
6. Apuntarse a un evento público con invitación pendiente la marca como aceptada.
7. El número de pendientes baja al aceptar y al rechazar.

## Cómo ejecutarlas

- **Sin emulador:** en Android Studio, clic derecho sobre la carpeta `app/src/test` > *Run Tests*, o
  `./gradlew testDebugUnitTest`.
- **Con emulador o móvil:** clic derecho sobre `app/src/androidTest` > *Run Tests*, o
  `./gradlew connectedDebugAndroidTest`.

## Resultados

Se anotan al ejecutarlas para el informe de pruebas del proyecto (fecha, versión y resultado):

| Fecha | Sin emulador | Con emulador | Notas |
|---|---|---|---|
| _pendiente_ | _pendiente_ | _pendiente_ | |

## Límites conocidos

- Aún no hay forma de salir de un evento (Issue 67), así que "liberar una plaza" solo se prueba con el DAO falso.
- La pantalla "Mis eventos" todavía no está hecha (Issue 63): se comprueba que las listas se actualizan
  (`observarEventosUnidos`), no la pantalla.
