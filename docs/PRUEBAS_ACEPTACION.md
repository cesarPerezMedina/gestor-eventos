# Pruebas de aceptación de usuario (Issue 81)

Recorridos de extremo a extremo para comprobar la versión candidata desde el punto de vista de quien usa la app.
Se hacen a mano en un móvil o emulador, con la app instalada desde cero. Al terminar, se rellena la columna
**Resultado** y se anotan los fallos en **Observaciones** (y en una Issue si hay que corregirlos).

## Cómo se ha probado

| Dato | Valor |
|---|---|
| Versión / commit probado | _pendiente_ |
| Dispositivo y Android | _pendiente_ |
| Idioma del móvil | Español (y una pasada en inglés, casos IDM) |
| Fecha y quién prueba | _pendiente_ |

**Preparación:** instalar la app desde cero (datos borrados). Crear tres usuarios para los casos con varias
personas: `ana_23`, `luis_9` y `marta`. Las pruebas con varios usuarios se hacen cerrando sesión y entrando con
el siguiente.

**Resultado:** `OK` (funciona como se espera), `FALLA` (no funciona, anotar qué pasa) o `N/A` (no se puede probar todavía).

## 1. Acceso y sesión

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| AU-1 | Registrar un usuario nuevo con datos válidos. | Se crea la cuenta y se entra en la app. | | |
| AU-2 | Registrarse con un correo o nombre de usuario ya existente. | Mensaje de error claro; no se crea otra cuenta. | | |
| AU-3 | Registrarse con contraseña débil o campos vacíos. | Se indica qué campo falla; no se avanza. | | |
| AU-4 | Iniciar sesión con credenciales correctas. | Entra en Inicio con su nombre de usuario. | | |
| AU-5 | Iniciar sesión con contraseña incorrecta. | Mensaje de error; no entra. | | |
| AU-6 | Cerrar la app del todo y volver a abrirla con la sesión iniciada. | Entra directamente, sin pedir login. | | |
| AU-7 | Cerrar sesión desde Ajustes. | Vuelve a Login; al reabrir la app pide login. | | |
| AU-8 | Recuperar contraseña desde Login. | Se puede restablecer y entrar con la nueva. | | |

## 2. Crear eventos

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| EV-1 | Crear un evento **público** con todos los campos. | Se guarda y aparece en Buscar para otros usuarios. | | |
| EV-2 | Crear un evento **privado** invitando a `ana_23` y `marta`. | Se guarda; no aparece en Buscar; ellas reciben una invitación. | | |
| EV-3 | Nombre de menos de 3 caracteres. | No se guarda; mensaje de error. | | |
| EV-4 | Máximo de participantes menor que el mínimo. | No se guarda; mensaje de error. | | |
| EV-5 | Elegir la ubicación en el mapa o por dirección. | La ubicación elegida se guarda con el evento. | | |
| EV-6 | Elegir fecha y hora. | Se muestran correctas y se guardan. | | |
| EV-7 | Pulsar Crear con campos obligatorios vacíos. | Se marcan los campos que faltan. | | |

## 3. Buscar y unirse

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| BU-1 | Abrir Buscar con eventos públicos creados. | Salen todos los públicos y ningún privado. | | |
| BU-2 | Filtrar por nombre. | Solo salen los que coinciden. | | |
| BU-3 | Filtrar por cercanía con permiso de ubicación. | Salen los cercanos, del más próximo al más lejano. | | |
| BU-4 | Denegar el permiso de ubicación y buscar por cercanía. | Aviso claro; la app no se cierra. | | |
| BU-5 | Unirse a un evento público con plazas. | Queda apuntado; el contador de plazas sube. | | |
| BU-6 | Intentar unirse a un evento completo. | No deja; indica que está completo. | | |
| BU-7 | Intentar unirse dos veces al mismo evento. | Indica que ya está apuntado. | | |

## 4. Invitaciones

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| IN-1 | Con `ana_23`, abrir la campana de Inicio. | Sale la invitación al evento privado, con quién invita y la fecha. | | |
| IN-2 | Aceptar la invitación. | Desaparece de la lista, sale un aviso y queda apuntada. | | |
| IN-3 | Con `marta`, rechazar la invitación. | Desaparece de la lista y no queda apuntada. | | |
| IN-4 | Aceptar con el evento ya completo (máximo alcanzado). | Aviso de que está completo; la invitación sigue pendiente. | | |
| IN-5 | Sin invitaciones pendientes. | Mensaje "No tienes invitaciones pendientes". | | |
| IN-6 | Pulsar Aceptar muchas veces seguidas. | Solo se apunta una vez. | | |

## 5. Salir y eliminar eventos

> La lógica está hecha y probada con tests automáticos (Issues 67 y 68), pero los botones dependen de la pantalla
> "Mis eventos" (Issues 63 y 70). Si todavía no están en la app, marcar `N/A`.

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| GE-1 | Salirse de un evento al que se unió. | Pide confirmación; al confirmar deja de estar apuntado y se libera la plaza. | | |
| GE-2 | El creador intenta salirse de su propio evento. | No puede; se le indica que puede eliminarlo. | | |
| GE-3 | Eliminar un evento propio. | Pide confirmación (botón en rojo); al confirmar desaparece para todos. | | |
| GE-4 | Un usuario que no es el creador intenta eliminarlo. | No puede. | | |

## 6. Mis eventos y amigos

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| ME-1 | Abrir "Mis eventos" con eventos creados y unidos. | Salen los creados y los unidos (según el estado de las Issues 63 y 64). | | |
| AM-1 | Buscar un usuario por nombre en Amigos. | Salen resultados que coinciden. | | |
| AM-2 | Enviar y aceptar una solicitud de amistad entre dos usuarios. | Quedan como amigos en ambas cuentas. | | |

## 7. Ajustes, diseño y accesibilidad

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| AJ-1 | Cambiar el tema (claro, oscuro, sistema). | Cambia en toda la app, no solo en Ajustes. | | |
| AJ-2 | Cambiar el tamaño del texto a "Muy grande" y recorrer las pantallas. | Los textos crecen y ninguna pantalla corta contenido ni botones. | | |
| AJ-3 | Cerrar y reabrir la app. | Se conservan el tema y el tamaño de texto. | | |
| AJ-4 | Abrir "Otros ajustes". | Se abre sin errores y las opciones responden. | | |
| AC-1 | Recorrer Login, Inicio y Crear evento con TalkBack. | Botones e iconos se leen con un nombre claro y en orden lógico. | | |
| AC-2 | Comprobar el contraste de textos en modo claro y oscuro. | Todo se lee bien, también con luz fuerte. | | |
| AC-3 | Zonas táctiles de botones principales. | Se pulsan sin fallar con el dedo. | | |
| IDM-1 | Cambiar el móvil a inglés y recorrer la app. | Los textos salen en inglés, sin recortes ni claves sin traducir. | | |

## 8. Robustez

| ID | Recorrido | Resultado esperado | Resultado | Observaciones |
|---|---|---|---|---|
| RB-1 | Girar la pantalla en Crear evento a medio rellenar. | No se pierden los datos ni se cierra la app. | | |
| RB-2 | Ir atrás y adelante entre pestañas de la barra inferior. | La navegación es coherente y no se duplican pantallas. | | |
| RB-3 | Dejar la app en segundo plano unos minutos y volver. | Sigue con la sesión y el estado. | | |
| RB-4 | Recorrido completo: registro, crear evento privado con invitados, aceptar con otro usuario, buscar. | Sin errores ni cierres inesperados. | | |

## Resumen para el informe

| | Total | OK | FALLA | N/A |
|---|---|---|---|---|
| Casos | 47 | | | |

**Fallos encontrados y qué se hizo** (una línea por fallo: ID, qué pasó, Issue o commit que lo corrige, o "pendiente"):

- _pendiente_

**Conclusión:** _pendiente_ (por ejemplo: "versión candidata apta para entregar" o qué falta corregir antes del viernes).
