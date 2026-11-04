package com.example.gestordeeventos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gestordeeventos.data.local.dao.EventoDao
import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.local.dao.ParticipacionDao
import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario

/**
 * Base de datos local de la app (archivo "gestor_eventos.db" en el propio móvil).
 *
 * Para añadir una tabla nueva: crear la clase con @Entity, añadirla a `entities`,
 * crear su DAO y exponerlo aquí con una función abstracta. Al cambiar las tablas hay que
 * subir `version` en 1.
 */
@Database(
    entities = [Usuario::class, Evento::class, Participacion::class, Invitacion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun eventoDao(): EventoDao
    abstract fun participacionDao(): ParticipacionDao
    abstract fun invitacionDao(): InvitacionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /** Devuelve la única instancia de la base de datos (se crea la primera vez que se pide). */
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_eventos.db"
                )
                    // Durante el desarrollo, si cambia el esquema se borra y se recrea la base de datos
                    // en vez de cerrarse la app. Se pierden los datos de prueba.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
