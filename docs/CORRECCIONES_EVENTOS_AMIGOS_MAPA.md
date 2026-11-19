# Correcciones de Eventos, Amigos y mapa

Segunda tanda de correcciones tras revisar la app en el móvil (la primera fue
[`MEJORAS_INICIO_Y_BUSCAR.md`](MEJORAS_INICIO_Y_BUSCAR.md)).

## Qué se ha cambiado

| # | Problema visto | Causa | Cambio |
|---|----------------|-------|--------|
| 1 | "Mis eventos" salía en inglés ("My events", "Upcoming", "Cancelled"…). También el selector Público/Privado al crear un evento. | `strings_mis_eventos.xml` y `strings_visibilidad.xml` tenían el inglés como idioma por defecto y no existía versión en español. | El idioma por defecto (`values/`) pasa a ser español y el inglés queda en `values-en/`, como el resto de pantallas. |
| 2 | Al abandonar un evento, no aparecía en "Cancelados". | Salir borraba la participación y no quedaba constancia de que el usuario había estado. | Nueva tabla `salidas`. Al salir, el evento pasa a **Unidos > Cancelados** con la etiqueta "Abandonado" y sin botón "Salir". Si el usuario vuelve a apuntarse, deja de estar abandonado. |
| 3 | Al enviar una solicitud de amistad a otra cuenta, no le llegaba. | El botón "Añadir" no estaba conectado a nada, y la pestaña "Solicitudes" no existía (solo cambiaba de color). | "Añadir" envía la solicitud. La pestaña **Solicitudes** muestra las recibidas con Aceptar / Rechazar y se actualiza sola. |
| 4 | Seguían saliendo invitaciones de ejemplo en Amigos. | Datos de ejemplo por defecto en la pantalla. | Se quitan. Las invitaciones a eventos reales se ven con la campana (pantalla Invitaciones). |
| 5 | El código QR no funcionaba (ni hace falta). | Botones sin función. | Se quitan "Escanear código QR" y "Ver mi código". |
| 6 | El mapa de "Crear evento" salía con carteles amarillos "403 Access blocked". | OpenStreetMap bloquea los identificadores de app genéricos y la app se identificaba como `com.example.gestordeeventos`. | La app se identifica con su nombre y la URL del proyecto (`MapaSelector.kt`). |

Además, en Amigos: la pantalla tiene ya **tres pestañas reales** (Mis Amigos / Buscar / ID / Solicitudes), el rótulo
de la barra superior dice "AMIGOS" (antes "INICIO"), "Tu ID" es el ID real del usuario (antes `#EVA-0281` fijo), se quita
el botón de perfil y la campana abre las invitaciones. El botón de mensajes avisa de que llegará próximamente.

## Cómo funcionan ahora

### Solicitudes de amistad
- Se escribe el **nombre de usuario** (filtro `@Usuario`) o el **ID numérico** (filtro `#ID`, con o sin `#`) y se pulsa "Añadir".
  Sale un aviso con el resultado ("Solicitud enviada a …", "Ya le has enviado una solicitud", etc.).
- La otra cuenta ve la solicitud en **Amigos > Solicitudes** (con un contador). Si la acepta, los dos se ven en "Mis Amigos";
  si la rechaza, se borra y se puede volver a enviar.
- Las solicitudes pendientes no cuentan como amigos hasta que se aceptan.

### Eventos abandonados
- Solo se registran los eventos de los que el usuario **se sale**. Si el creador elimina el evento, desaparece también de
  los abandonados de los demás.

## Cómo probarlo a mano
1. **Idioma:** con el móvil en español, "Mis eventos" y el selector Público/Privado de "Crear evento" salen en español.
2. **Abandonar:** con el usuario B, únete a un evento público del usuario A y pulsa "Salir" en Mis eventos > Unidos.
   Entra en el chip **Cancelados**: sale con la etiqueta "Abandonado" y solo con el botón "Ver".
3. **Solicitudes:** con el usuario A, en Amigos > Buscar / ID escribe el nombre de B y pulsa "Añadir". Entra con B:
   Amigos > Solicitudes muestra la solicitud de A. Acéptala y comprueba que ambos se ven en "Mis Amigos".
4. **Mapa:** en "Crear evento", el mapa debe mostrar calles, no carteles amarillos (necesita internet).

> **Base de datos:** esta Issue añade una tabla (`salidas`), así que la versión de la base de datos pasa de 2 a 3. Mientras
> la app esté en desarrollo, Room recrea la base de datos al cambiar la versión: **la primera vez que abras la app tras
> actualizar se borrarán los usuarios y eventos de prueba**, y habrá que volver a registrarse.
>
> **Mapa:** si siguiera sin cargar, desinstala la app del emulador y vuelve a instalarla, para vaciar la caché de mosaicos.

## Pruebas
- `MisEventosAbandonadosTest` (JVM): un evento abandonado cae en Cancelados, con su etiqueta, aunque su fecha ya pasara.
- `EventosAbandonadosTest` (androidTest, Room real): salir lo registra, volver a apuntarse lo quita, no se duplica y se borra
  con el evento.
- `SolicitudesAmistadFlujoTest` (androidTest, Room real): enviar, ver en Solicitudes, aceptar, rechazar, ID con `#` y no duplicar.
- `AmigosScreenTest` (androidTest): revisado para las tres pestañas, sin ejemplos ni QR.
- `AmistadRepositorioTest`: el doble de prueba del DAO implementa la consulta nueva.
