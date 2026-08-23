package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EnsayoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class RobotDatabase : RoomDatabase() {

    abstract fun ensayoDao(): EnsayoDao

    companion object {

        @Volatile
        private var INSTANCIA: RobotDatabase? = null

        /**
         * Patron singleton con doble verificacion.
         *
         * Abrir dos conexiones a la misma base de datos causa
         * bloqueos y corrupcion. Este patron garantiza una sola
         * instancia aunque varios hilos la pidan a la vez.
         */
        fun obtener(context: Context): RobotDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    RobotDatabase::class.java,
                    "robot_monitor.db"
                ).build().also { INSTANCIA = it }
            }
    }
}
