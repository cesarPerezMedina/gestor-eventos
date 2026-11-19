# Modo oscuro en toda la aplicación

## Problema
El interruptor de Ajustes > "Tema oscuro" solo cambiaba la pantalla de Ajustes. El resto de pantallas
(Inicio, Buscar, Eventos, Amigos, Crear evento, Invitaciones, Acceso) definen su paleta con colores fijos
claros (`val IniPagina = Color(0xFFF6F4FF)`, `Color.White`…), así que no seguían el tema de Material.

## Solución
- Nuevo `ui/theme/PaletaApp.kt`: `PaletaApp.oscuro` (estado de Compose) y `PaletaApp.elegir(claro, oscuro)`.
- `GestorDeEventosTheme` actualiza `PaletaApp.oscuro` con el valor que llega de MainActivity (el de Ajustes).
- Cada color fijo de las pantallas pasa a `val X get() = PaletaApp.elegir(<claro>, <oscuro>)`. El uso en el
  código no cambia, y el modo claro queda exactamente igual.
- `Color.White` usado como fondo de tarjetas, campos y barras pasa a `SuperficieApp`. Los textos blancos sobre
  botones morados siguen en blanco.

## Paleta oscura
Fondo `#15131F`, tarjetas `#1E1B2E`, campos `#25223A`, líneas `#363350`, texto `#ECEAFB`, texto secundario `#B9BDD3`,
morado de acento `#8A5CEB`/`#A78BFA`.

## Pendiente / límites
- Las teselas del mapa (OpenStreetMap) se ven siempre claras.
- Colores nuevos: usar `PaletaApp.elegir(claro, oscuro)` o `SuperficieApp`, no `Color(0x…)` sueltos.
