package com.example.myapplication

import android.app.Application
import com.example.myapplication.di.AppContainer

/**
 * Punto de entrada de la app.
 *
 * Vive mientras vive el proceso, asi que es el lugar natural para
 * el contenedor de dependencias.
 *
 * Debe estar declarada en el AndroidManifest con
 * android:name=".RobotMonitorApp" o no se ejecuta.
 */
class RobotMonitorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
