package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.*
import com.example.myapplication.domain.model.Preferencias
import com.example.myapplication.ui.navigation.NavegacionRobot
import com.example.myapplication.ui.screens.splash.PantallaSplash
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RobotMonitorApp).container

        setContent {
            val prefs by container.preferenciasRepository.preferencias
                .collectAsState(initial = Preferencias())

            var mostrarSplash by remember { mutableStateOf(true) }

            // El splash siempre en oscuro: es la identidad de marca.
            MyApplicationTheme(darkTheme = if (mostrarSplash) true else prefs.modoOscuro) {

                AnimatedVisibility(
                    visible = mostrarSplash,
                    exit = fadeOut(animationSpec = tween(450))
                ) {
                    PantallaSplash(onTerminado = { mostrarSplash = false })
                }

                AnimatedVisibility(
                    visible = !mostrarSplash,
                    enter = fadeIn(tween(500)) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(500)
                    )
                ) {
                    NavegacionRobot(container = container)
                }
            }
        }
    }
}
