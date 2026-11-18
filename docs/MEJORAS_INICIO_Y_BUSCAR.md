# Mejoras de Inicio y Buscar

Correcciones tras revisar la app en el móvil. Afectan a las pantallas **Inicio** y **Buscar**; el resto de pantallas
se corrigen en una segunda tanda.

## Qué se ha cambiado

| # | Problema visto | Cambio |
|---|----------------|--------|
| 1 | El botón redondo con forma de persona (arriba a la derecha en Inicio) ya no se usa: cerrar sesión y demás están en la rueda de Ajustes. | Se quita el botón de Inicio. |
| 2 | Los botones "Unirme" de Inicio no hacían nada: eran eventos de ejemplo. | Inicio enseña **eventos públicos reales** a los que se puede uno unir. Al pulsar "Unirme" el usuario se apunta y sale un aviso. |
| 3 | La barra inferior aparecía **duplicada** en Inicio. | Inicio ya no dibuja su propia barra; queda solo la de la app (`AppNavHost`). |
| 4 | Lo mismo en Buscar: eventos de ejemplo cuyos botones no hacían nada. | Buscar estaba sin conectar a la base de datos. Ahora usa `BuscarEventoRoute` + `BuscarViewModel`, así que enseña los eventos públicos reales, con su botón "Unirme" funcionando. Ya no quedan eventos de ejemplo en la app (solo en las vistas previas de Android Studio). |
| 5 | Había precios ("3,50 €", "Gratis", "COSTE", "p/p"). La app es gratuita. | Se elimina todo lo relacionado con precios de las tarjetas (`precio` y `precioNota` ya no existen). |
| 6 | Los filtros de arriba de Buscar no funcionaban. | Ahora funcionan: buscador de texto, chips de deporte, pestañas "Cerca de mí" / "Por nombre" y radio. El botón de ajustes (`Tune`) y el botón "Distancia" no hacían nada y se han quitado/convertido en una etiqueta ("Orden: Distancia / Fecha / A-Z"). |

## Cómo funcionan ahora

### Inicio
- **Tu agenda deportiva:** el evento más próximo (que no haya empezado) entre los que he creado y a los que me he unido.
  Si no hay ninguno, la tarjeta no se enseña.
- **Eventos disponibles:** eventos públicos publicados, que no han empezado, que no he creado yo, a los que no estoy
  apuntado y que no están completos. Del más próximo al más lejano, máximo 10.
- Los chips (Todos, Fútbol, Pádel, Baloncesto, Tenis, Atletismo) filtran esa lista.
- Si no hay eventos, se enseña un mensaje en lugar de una lista vacía.
- La lista y la tarjeta se actualizan solas (Flow de Room) al crear un evento o unirse a uno.
- Lógica en `InicioViewModel`; las reglas de qué sale están en `seleccionarSugerencias` y `seleccionarProximo`.

### Buscar
- **Buscador:** el evento debe contener todas las palabras escritas en el título o en el lugar. No distingue mayúsculas ni tildes.
- **Chips de deporte:** dejan solo ese deporte. El número de cada chip es el total de eventos de ese deporte.
- **Cerca de mí:** con permiso de ubicación, solo eventos dentro del radio elegido, del más cercano al más lejano.
  Sin ubicación, se enseñan todos los públicos por fecha y un aviso explica por qué.
- **Por nombre:** todos los públicos, en orden alfabético.
- **Mensajes cuando no hay nada:** "No hay eventos con estos filtros", "No hay eventos en este radio…" o
  "Todavía no hay eventos públicos".
- Lógica de filtrado en `filtrarEventos` (`ui/buscar/FiltrarEventos.kt`).

## Botones que todavía no tienen pantalla
Estos botones ya no hacen "nada" en silencio: enseñan un aviso de "próximamente".
- **Mapa** (Inicio y Buscar), **Mensajes** (Inicio) y **detalle de un evento** (tocar una tarjeta en Buscar).

## Cómo probarlo a mano
1. Con el usuario A, crea un evento **público** en el futuro (Crear evento).
2. Cierra sesión y entra con el usuario B. En **Inicio** debe salir el evento de A en "Eventos disponibles" y **no**
   debe salir en "Tu agenda deportiva".
3. Pulsa **Unirme**: sale "Te has apuntado al evento", la tarjeta desaparece de Inicio y en Buscar pasa a "Ya apuntado".
   "Tu agenda deportiva" muestra ahora ese evento.
4. En **Buscar**: escribe parte del nombre, pulsa un chip de deporte y cambia entre "Cerca de mí" y "Por nombre".
5. Comprueba que en Inicio hay **una sola** barra inferior, que no hay botón de perfil y que no aparece ningún precio.

> En el emulador, "Cerca de mí" necesita una ubicación: abre *Extended controls → Location* y fija un punto.
> Si el evento se creó lejos de ese punto, solo se verá en "Por nombre" o ampliando el radio.

## Pruebas
- `FiltrarEventosTest` (JVM): texto, tildes, varias palabras, lugar, deporte, combinación y orden por nombre.
- `InicioSugerenciasTest` (JVM): qué eventos son sugerencia (propios, apuntados, completos, pasados, privados,
  cancelados), orden, límite, avisos de plazas y próximo evento.
- `InicioYBuscarPantallaTest` (androidTest): sin barra inferior propia ni botón de perfil en Inicio, "Unirme" avisa,
  los chips y el buscador filtran en Buscar, sin precios y con el contador al día.
