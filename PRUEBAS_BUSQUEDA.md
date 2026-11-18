# Pruebas de los filtros de búsqueda (Issue 60)

Pruebas de los dos filtros de "Buscar eventos": por **nombre** y por **cercanía**. Casi todas se ejecutan sin
emulador (`./gradlew test`).

## Qué se prueba

| Archivo | Dónde se ejecuta | Qué comprueba |
|---|---|---|
| `FiltrarPorNombreTest` | Sin emulador | `filtrarPorNombre`: consulta vacía, mayúsculas y tildes (también la ñ), parte de una palabra, varias palabras en cualquier orden, espacios sobrantes, sin resultados y orden original. Solo mira el título, no el lugar. |
| `CercaniaTest` | Sin emulador | `distanciaKm` (ciudades conocidas, simetría, meridiano opuesto, polos) y `cajaDelimitadora` (centro, tamaño, ancho según la latitud, cerca de los polos, radio 0, y que todo punto dentro del radio cae dentro de la caja). |
| `EventosCercaTest` | Sin emulador | `EventoRepositorio.eventosPublicosCerca`: solo lo que está dentro del radio, del más cercano al más lejano, distancia exacta, esquina de la caja fuera del círculo y radio 0. |
| `BuscarViewModelTest` | Sin emulador | Lo que ve la pantalla: sin ubicación salen todos los públicos por fecha, "Cerca de mí" con cada radio, "Por nombre", privados descartados, actualización en vivo y chips de deporte. |
| `BusquedaEventosDaoTest` | Emulador o móvil | La SQL real de `observarPublicosEnCaja`: solo públicos, bordes incluidos, eventos sin punto en el mapa fuera, apuntados y "yo apuntado". |

`FakeEventoDao` (en `src/test`) es una base de datos falsa en memoria que imita las consultas de Buscar, igual que
`FakeInvitacionDao` hace con las invitaciones.

## Cómo ejecutarlas

- Sin emulador: `./gradlew test`, o abrir cada archivo en Android Studio y pulsar ejecutar.
- Con emulador: abrir `BusquedaEventosDaoTest` y pulsar ejecutar.
