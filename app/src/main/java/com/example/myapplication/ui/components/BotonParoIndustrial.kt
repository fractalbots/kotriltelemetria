package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

/**
 * Boton de paro de emergencia tipo seta industrial.
 *
 * Es el mismo lenguaje visual que los pulsadores rojos de las
 * maquinas: circular, con relieve, ranuras radiales y aro
 * metalico. Se dibuja entero en Canvas.
 *
 * Cuando esta activo late en rojo para que se vea desde lejos.
 * Al pulsarlo se hunde con animacion y vibra.
 */
@Composable
fun BotonParoIndustrial(
    activo: Boolean,
    tamano: Dp = 130.dp,
    hapticaActiva: Boolean = true,
    onClick: () -> Unit
) {
    var presionado by remember { mutableStateOf(false) }
    val haptica = LocalHapticFeedback.current

    val hundido by animateFloatAsState(
        targetValue = if (presionado || activo) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f),
        label = "hundido"
    )

    val t = rememberInfiniteTransition(label = "alerta")
    val latido by t.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "latido"
    )

    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "escala"
    )

    Box(
        modifier = Modifier
            .size(tamano)
            .scale(escala)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        presionado = true
                        if (hapticaActiva) {
                            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        tryAwaitRelease()
                        presionado = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {

        Canvas(Modifier.size(tamano)) {

            val centro = center
            val r = size.minDimension / 2f

            // Halo de alerta cuando esta activo
            if (activo) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KotrilRojo.copy(alpha = 0.45f * latido),
                            Color.Transparent
                        ),
                        center = centro,
                        radius = r
                    ),
                    radius = r,
                    center = centro
                )
            }

            // Base metalica exterior
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF3A4658),
                        Color(0xFF1A2333),
                        Color(0xFF44526A)
                    ),
                    start = Offset(centro.x - r, centro.y - r),
                    end = Offset(centro.x + r, centro.y + r)
                ),
                radius = r * 0.94f,
                center = centro
            )

            // Aro interior de contraste
            drawCircle(
                color = KotrilNegro,
                radius = r * 0.80f,
                center = centro
            )

            // Ranuras radiales del aro
            for (i in 0 until 24) {
                val ang = Math.toRadians(i * 15.0)
                val dx = kotlin.math.cos(ang).toFloat()
                val dy = kotlin.math.sin(ang).toFloat()
                drawLine(
                    color = Color.Black.copy(alpha = 0.55f),
                    start = centro + Offset(dx * r * 0.82f, dy * r * 0.82f),
                    end = centro + Offset(dx * r * 0.92f, dy * r * 0.92f),
                    strokeWidth = 2.4f
                )
            }

            // Sombra bajo la seta (se reduce al hundirse)
            val elevacion = (1f - hundido) * r * 0.07f
            drawCircle(
                color = Color.Black.copy(alpha = 0.65f),
                radius = r * 0.74f,
                center = centro + Offset(0f, elevacion + r * 0.03f)
            )

            // Cuerpo de la seta
            val colorArriba = if (activo) Color(0xFFFF5470) else Color(0xFFE8213F)
            val colorAbajo  = if (activo) Color(0xFF8B0F23) else Color(0xFF6B0A19)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colorArriba, Color(0xFFC01130), colorAbajo),
                    center = centro - Offset(r * 0.22f, r * 0.28f + elevacion),
                    radius = r * 1.25f
                ),
                radius = r * 0.72f,
                center = centro - Offset(0f, elevacion)
            )

            // Borde superior de la seta
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = r * 0.72f,
                center = centro - Offset(0f, elevacion),
                style = Stroke(width = 2f)
            )

            // Anillo grabado
            drawCircle(
                color = Color.Black.copy(alpha = 0.28f),
                radius = r * 0.55f,
                center = centro - Offset(0f, elevacion),
                style = Stroke(width = 2.5f)
            )

            // Brillo especular
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(
                    centro.x - r * 0.42f,
                    centro.y - r * 0.58f - elevacion
                ),
                size = Size(r * 0.60f, r * 0.34f)
            )

            // Marcas triangulares de advertencia en el aro
            for (i in 0 until 4) {
                val ang = Math.toRadians(45.0 + i * 90.0)
                val dx = kotlin.math.cos(ang).toFloat()
                val dy = kotlin.math.sin(ang).toFloat()
                val p = centro + Offset(dx * r * 0.86f, dy * r * 0.86f)
                drawCircle(
                    color = if (activo) KotrilRojo.copy(alpha = latido)
                            else KotrilAmbar.copy(alpha = 0.75f),
                    radius = 3.2f,
                    center = p
                )
            }
        }

        // Texto sobre la seta
        Text(
            text = if (activo) "ACTIVO" else "STOP",
            color = Color.White,
            fontSize = if (activo) 13.sp else 17.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.scale(1f - hundido * 0.04f)
        )
    }
}
