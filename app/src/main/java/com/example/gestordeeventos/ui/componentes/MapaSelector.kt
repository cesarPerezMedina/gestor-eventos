package com.example.gestordeeventos.ui.componentes

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gestordeeventos.data.localizacion.Ubicacion
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

/** Centro del mapa mientras no hay ningún punto elegido (Puerta del Sol, Madrid). */
private val CENTRO_INICIAL = GeoPoint(40.4168, -3.7038)

private const val ZOOM_INICIAL = 15.0

/**
 * Mapa interactivo (OpenStreetMap, sin clave de API) para elegir el punto de un evento (Issue 46).
 *
 * El usuario toca el mapa para colocar el marcador y se avisa en [onUbicacionElegida]. Es un componente sin
 * estado: quien lo usa guarda el punto y lo vuelve a pasar en [ubicacion]; cuando cambia, el mapa se centra
 * en él (por ejemplo, tras "usar mi ubicación" o buscar una dirección).
 *
 * Funciona dentro de una pantalla con scroll: mientras el dedo está sobre el mapa, la pantalla no se desplaza.
 */
@Composable
fun MapaSelector(
    ubicacion: Ubicacion?,
    onUbicacionElegida: (Ubicacion) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Las vistas previas de Android Studio no pueden cargar un MapView: se dibuja un recuadro en su lugar
    if (LocalInspectionMode.current) {
        Box(modifier.background(Color(0xFFE7EAFB)), contentAlignment = Alignment.Center) {
            Text("Mapa")
        }
        return
    }

    val context = LocalContext.current
    val alElegir by rememberUpdatedState(onUbicacionElegida)

    val mapa = remember {
        configurarOsmdroid(context)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(ZOOM_INICIAL)
            controller.setCenter(CENTRO_INICIAL)

            // Un toque en el mapa elige el punto
            overlays.add(
                MapEventsOverlay(
                    object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            alElegir(Ubicacion(latitud = p.latitude, longitud = p.longitude))
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    }
                )
            )

            // Dentro de un scroll vertical, el mapa se queda con el gesto mientras se toca
            setOnTouchListener { vista, evento ->
                if (evento.action == MotionEvent.ACTION_DOWN || evento.action == MotionEvent.ACTION_MOVE) {
                    vista.parent?.requestDisallowInterceptTouchEvent(true)
                }
                false
            }
        }
    }

    // El mapa necesita saber cuándo la pantalla se pausa y se reanuda
    val propietario = LocalLifecycleOwner.current
    DisposableEffect(propietario, mapa) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapa.onResume()
                Lifecycle.Event.ON_PAUSE -> mapa.onPause()
                else -> Unit
            }
        }
        propietario.lifecycle.addObserver(observador)
        onDispose {
            propietario.lifecycle.removeObserver(observador)
            mapa.onPause()
            mapa.onDetach()
        }
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier,
        update = { vista ->
            vista.overlays.removeAll { it is Marker }
            if (ubicacion != null) {
                val punto = GeoPoint(ubicacion.latitud, ubicacion.longitud)
                vista.overlays.add(
                    Marker(vista).apply {
                        position = punto
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ -> true } // sin globo de información
                    }
                )
                // Solo se recentra cuando el punto cambia, no en cada redibujado
                if (vista.tag != ubicacion) {
                    vista.controller.animateTo(punto)
                    vista.tag = ubicacion
                }
            } else {
                vista.tag = null
            }
            vista.invalidate()
        },
    )
}

/**
 * osmdroid necesita un identificador de la app (si no, los servidores de mapas pueden bloquear las peticiones)
 * y un sitio donde guardar los mosaicos. Se usa la caché de la app para no necesitar permiso de almacenamiento.
 */
private fun configurarOsmdroid(context: Context) {
    val configuracion = Configuration.getInstance()
    configuracion.userAgentValue = context.packageName
    val base = File(context.cacheDir, "osmdroid")
    configuracion.osmdroidBasePath = base
    configuracion.osmdroidTileCache = File(base, "tiles")
}