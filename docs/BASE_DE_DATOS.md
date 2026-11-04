# Base de datos local (Issue 4)

La app guarda los datos en el propio móvil con **Room**, una capa sobre SQLite. Es suficiente para
probar la app en clase: los usuarios, eventos e invitaciones existen en el dispositivo donde se usa la app.
Hay que tener en cuenta que dos móviles distintos NO comparten datos entre sí.

## Cómo está organizado

```
data/
  model/    Usuario, Evento, Participacion, Invitacion, Deporte   (cada clase con @Entity es una tabla)
  local/
    AppDatabase.kt        la base de datos (archivo gestor_eventos.db)
    dao/                  las consultas de cada tabla
  repositorio/            usan los DAO (se rellenan en las Issues de cada módulo)
```

| Tabla | Para qué sirve |
|---|---|
| `usuarios` | Cuentas. Correo y nombre de usuario únicos. |
| `eventos` | Eventos, con su creador (`creadorId`). |
| `participaciones` | Quién participa en qué evento (relación muchos a muchos). |
| `invitaciones` | Invitaciones a eventos, con estado PENDIENTE, ACEPTADA o RECHAZADA. |

Las claves foráneas están en cascada: si se borra un evento, se borran sus participaciones e invitaciones.
La tabla de amistades se creará en la Issue 29.

## Cómo usarla desde el código

```kotlin
val db = AppDatabase.obtener(context)          // siempre la misma instancia
val id = db.usuarioDao().insertar(usuario)     // las funciones del DAO son suspend: se llaman desde una corrutina
val usuario = db.usuarioDao().buscarPorId(id)
```

Normalmente la pantalla no usa el DAO directamente: el ViewModel llama al repositorio y el repositorio usa el DAO.

## Cómo añadir algo en una Issue posterior

- **Una consulta nueva:** añadir una función con `@Query` en el DAO que corresponda.
- **Una tabla nueva:** crear la clase con `@Entity` en `data/model`, añadirla a `entities` en `AppDatabase`,
  crear su DAO y exponerlo con una función abstracta en `AppDatabase`.
- **Cualquier cambio en las tablas:** subir `version` en `AppDatabase` (de 1 a 2, etc.).
  Mientras se desarrolla, la base de datos se borra y se vuelve a crear sola al cambiar la versión,
  así que se pierden los datos de prueba.

## Ver los datos mientras se prueba

Con la app ejecutándose en el emulador: **View → Tool Windows → App Inspection → Database Inspector**.
Permite ver las tablas y ejecutar consultas.

## Pruebas

`app/src/androidTest/.../AppDatabaseTest.kt` comprueba que se puede guardar y leer un usuario, que no se
repite el correo y que el borrado en cascada funciona. Se ejecuta con el botón verde junto a la clase y
necesita un emulador o un móvil conectado.
