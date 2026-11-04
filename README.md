Gestor de Eventos Deportivos

Encuentra. Organiza. Juega.

Aplicación diseñada para crear, buscar y gestionar eventos deportivos, conectando a personas de una misma zona para practicar deporte juntas.

Funcionalidades principales:

    - Registro e inicio de sesión: creación de cuentas y recuperación de contraseñas.

    - Crear eventos: organizar actividades deportivas indicando nombre, descripción, fecha, hora, ubicación y número de participantes.
 
    - Buscar eventos: encontrar eventos públicos por nombre o proximidad.

    - Mis eventos: consultar, abandonar o eliminar eventos.

    - Invitaciones: aceptar o rechazar invitaciones a eventos.

    - Amigos: añadir usuarios mediante su ID o nombre de usuario.

    - Ajustes: personalizar el tema claro u oscuro y el tamaño del texto.

    - Cerrar sesión: salir de la cuenta.

Deportes disponibles:

    -Fútbol, baloncesto, tenis, pádel, atletismo y otras disciplinas deportivas.

Tipos de eventos:

    -Públicos: visibles en las búsquedas y recomendaciones de eventos cercanos.

    -Privados: accesibles mediante invitación o autorización.

Ubicación y gestión:

    -La aplicación utilizará la ubicación del usuario, con su permiso, para recomendar eventos públicos cercanos. Cada evento podrá incluir una dirección o un punto seleccionado en un mapa.

    -Los eventos cuya fecha haya pasado dejarán de aparecer en la lista de eventos activos.

Posibles mejoras futuras:

    -Notificaciones y recordatorios.

    -Chat entre participantes.

    -Valoraciones de eventos.

    -Calendario deportivo personal.

Autores:

-Iñaki Marín

-Carlos Martín

-César Pérez

Estado del proyecto: En fase de diseño y desarrollo.

El deporte se disfruta más cuando se comparte.


1. Estructura del sistema de diseño
   El sistema se organiza en los siguientes archivos, cada uno con una responsabilidad específica:

Archivo

Funcionalidad

Color.kt

Define la paleta de colores corporativa para los modos claro y oscuro.

Type.kt

Configura la tipografía Poppins y los diferentes estilos tipográficos.

Shape.kt

Establece las formas y los radios de las esquinas de los componentes.

Espaciado.kt

Centraliza los valores de espaciado (Espacio) y dimensiones (Tamano).

Theme.kt

Integra los elementos del sistema y gestiona los modos claro y oscuro, así como la escala del texto.

VistaPreviaTema.kt

Proporciona un catálogo de componentes mediante funciones @Preview para facilitar la revisión visual del diseño.

2. Paleta de colores corporativa
   Se establece una paleta cromática basada en los colores verde, ámbar y azul, que constituyen los principales elementos de identidad visual de la aplicación.

La paleta contempla dos configuraciones, correspondientes al modo claro y al modo oscuro, con el propósito de garantizar una presentación adecuada en ambos entornos.

Rol

Modo claro

Modo oscuro

Aplicación

Primario

#0A7B43

#6FDC98

Identidad corporativa, botones principales y barra de navegación inferior.

Secundario

#F59E0B

#FFB84D

Elementos destacados, avisos y estados de los eventos.

Terciario

#1D5FBF

#A9C7FF

Información, enlaces, mapas y eventos privados.

Fondo

#F6F8F5

#0F1511

Fondo general de las pantallas.

Superficie

#FFFFFF

#151B17

Tarjetas, ventanas y cuadros de diálogo.

Texto

#17211B

#E1E9E2

Contenido textual principal.

Error

#BA1A1A

#FFB4AB

Mensajes de error y acciones destructivas.

Asimismo, se tiene en consideración la accesibilidad visual, estableciendo como criterio que las combinaciones de texto y fondo alcancen un contraste mínimo de 4,5:1, correspondiente al nivel AA de las pautas WCAG.

3. Sistema tipográfico
   La tipografía principal seleccionada para la aplicación es Poppins, incorporada al proyecto mediante los recursos disponibles en res/font.

Se utilizan tres variantes tipográficas:

Regular.

Medium.

Bold.