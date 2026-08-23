package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Joystick estilo mando de consola.
 *
 * Capas de dibujo, de atras hacia adelante:
 *   1. Halo exterior que se enciende segun la intensidad
 *   2. Base hundida con gradiente radial (efecto concavo)
 *   3. Anillo de borde con brillo
 *   4. Marcas de los ejes
 *   5. Arco direccional que indica hacia donde apunta
 *   6. Perilla con gradiente y sombra proyectada
 *   7. Reflejo especular en la perilla
 *
 * Incluye vibracion haptica al empezar a mover y al soltar,
 * que es lo que le da tacto de mando fisico.
 */
@Composable
fun JoystickPro(
    modifier: Modifier = Modifier,
    tamano: Dp = 200.dp,
    soloHorizontal: Boolean = false,
    colorAcento: Color = KotrilCian,
    habilitado: Boolean = true,
    onCambio: (x: Float, y: Float) -> Unit
) {
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    var arrastrando by remember { mutableStateOf(false) }

    val haptica = LocalHapticFeedback.current

    // Al soltar, la perilla vuelve al centro con rebote
    val animX by animateFloatAsState(
        targetValue = desplazamiento.x,
        animationSpec = if (arrastrando) spring(stiffness = Spring.StiffnessHigh)
                        else spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "animX"
    )
    val animY by animateFloatAsState(
        targetValue = desplazamiento.y,
        animationSpec = if (arrastrando) spring(stiffness = Spring.StiffnessHigh)
                        else spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "animY"
    )

    val intensidadObjetivo = if (arrastrando) 1f else 0f
    val intensidad by animateFloatAsState(
        targetValue = intensidadObjetivo,
        animationSpec = spring(),
        label = "intensidad"
    )

    Box(
        modifier = modifier
            .size(tamano)
            .pointerInput(soloHorizontal, habilitado) {
                if (!habilitado) return@pointerInput

                val radioMax = size.width / 2f * 0.68f

                detectDragGestures(
                    onDragStart = {
                        arrastrando = true
                        haptica.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = {
                        arrastrando = false
                        desplazamiento = Offset.Zero
                        onCambio(0f, 0f)
                        haptica.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDragCancel = {
                        arrastrando = false
                        desplazamiento = Offset.Zero
                        onCambio(0f, 0f)
                    }
                ) { cambio, arrastre ->
                    cambio.consume()

                    val nuevo = desplazamiento + arrastre
                    val limitado = if (soloHorizontal) {
                        Offset(nuevo.x.coerceIn(-radioMax, radioMax), 0f)
                    } else {
                        val d = hypot(nuevo.x, nuevo.y)
                        if (d > radioMax) nuevo * (radioMax / d) else nuevo
                    }

                    desplazamiento = limitado

                    // Y invertido: arriba = adelante
                    onCambio(
                        (limitado.x / radioMax).coerceIn(-1f, 1f),
                        (-limitado.y / radioMax).coerceIn(-1f, 1f)
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.size(tamano)) {

            val centro = center
            val radioTotal = size.minDimension / 2f
            val radioBase = radioTotal * 0.92f
            val radioPerilla = radioTotal * 0.30f
            val pos = centro + Offset(animX, animY)
            val distancia = hypot(animX, animY)
            val radioMax = radioTotal * 0.68f
            val factor = (distancia / radioMax).coerceIn(0f, 1f)

            // 1) Halo exterior
            if (intensidad > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorAcento.copy(alpha = 0.30f * intensidad),
                            Color.Transparent
                        ),
                        center = centro,
                        radius = radioTotal
                    ),
                    radius = radioTotal,
                    center = centro
                )
            }

            // 2) Base hundida
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        KotrilNegro,
                        KotrilSuperficie2,
                        KotrilSuperficie
                    ),
                    center = centro,
                    radius = radioBase
                ),
                radius = radioBase,
                center = centro
            )

            // 3) Anillo de borde
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        KotrilBorde,
                        colorAcento.copy(alpha = 0.45f + 0.55f * intensidad),
                        KotrilBorde,
                        colorAcento.copy(alpha = 0.25f + 0.45f * intensidad),
                        KotrilBorde
                    ),
                    center = centro
                ),
                radius = radioBase,
                center = centro,
                style = Stroke(width = 3.5f)
            )

            // Anillo interior guia
            drawCircle(
                color = KotrilBorde.copy(alpha = 0.55f),
                radius = radioMax,
                center = centro,
                style = Stroke(width = 1.2f)
            )

            // 4) Marcas de eje
            val marca = radioBase * 0.13f
            val ejes = if (soloHorizontal) listOf(0f, 180f)
                       else listOf(0f, 90f, 180f, 270f)

            ejes.forEach { grados ->
                val rad = Math.toRadians(grados.toDouble())
                val dx = kotlin.math.cos(rad).toFloat()
                val dy = kotlin.math.sin(rad).toFloat()
                drawLine(
                    color = KotrilTextoBajo.copy(alpha = 0.55f),
                    start = centro + Offset(dx * (radioBase - marca), dy * (radioBase - marca)),
                    end   = centro + Offset(dx * (radioBase - 8f), dy * (radioBase - 8f)),
                    strokeWidth = 2.2f
                )
            }

            // 5) Arco direccional
            if (factor > 0.08f) {
                val anguloRad = atan2(animY, animX)
                val anguloGrados = Math.toDegrees(anguloRad.toDouble()).toFloat()
                val barrido = 54f

                drawArc(
                    color = colorAcento.copy(alpha = 0.20f + 0.55f * factor),
                    startAngle = anguloGrados - barrido / 2,
                    sweepAngle = barrido,
                    useCenter = false,
                    topLeft = Offset(centro.x - radioBase, centro.y - radioBase),
                    size = androidx.compose.ui.geometry.Size(radioBase * 2, radioBase * 2),
                    style = Stroke(width = 6f)
                )
            }

            // 6) Sombra proyectada de la perilla
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = radioPerilla * 1.05f,
                center = pos + Offset(0f, radioPerilla * 0.16f)
            )

            // Perilla
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        KotrilCianClaro,
                        colorAcento,
                        KotrilCianOscuro
                    ),
                    center = pos - Offset(radioPerilla * 0.3f, radioPerilla * 0.35f),
                    radius = radioPerilla * 1.8f
                ),
                radius = radioPerilla,
                center = pos
            )

            // Borde de la perilla
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = radioPerilla,
                center = pos,
                style = Stroke(width = 1.6f)
            )

            // Anillo concentrico interno
            drawCircle(
                color = KotrilNegro.copy(alpha = 0.35f),
                radius = radioPerilla * 0.62f,
                center = pos,
                style = Stroke(width = 1.4f)
            )

            // 7) Reflejo especular
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.Transparent
                    ),
                    center = pos - Offset(radioPerilla * 0.32f, radioPerilla * 0.38f),
                    radius = radioPerilla * 0.62f
                ),
                radius = radioPerilla * 0.55f,
                center = pos - Offset(radioPerilla * 0.28f, radioPerilla * 0.32f)
            )

            // Punto central de la perilla
            drawCircle(
                color = KotrilNegro.copy(alpha = 0.55f),
                radius = radioPerilla * 0.12f,
                center = pos
            )

            if (!habilitado) {
                drawCircle(
                    color = KotrilNegro.copy(alpha = 0.6f),
                    radius = radioBase,
                    center = centro
                )
            }
        }
    }
}

/** Redondea a un decimal, para que los digitos no tiemblen. */
fun redondear1(v: Float): Float = (v * 10).roundToInt() / 10f
