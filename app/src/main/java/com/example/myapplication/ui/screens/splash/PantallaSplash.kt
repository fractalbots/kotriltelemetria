package com.example.myapplication.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Pantalla de arranque.
 *
 * Composicion de animaciones:
 *   - El logo entra con rebote (overshoot) y se desvanece
 *   - Un anillo exterior gira continuamente
 *   - Un halo cian pulsa detras del logo
 *   - Una barra de progreso avanza durante los 2 segundos
 *   - Un barrido de luz cruza el titulo
 *
 * Todo se ejecuta en el hilo de animacion de Compose, sin
 * bloquear la interfaz.
 */
@Composable
fun PantallaSplash(onTerminado: () -> Unit) {

    var iniciado by remember { mutableStateOf(false) }

    // Entrada del logo con rebote
    val escalaLogo by animateFloatAsState(
        targetValue = if (iniciado) 1f else 0.35f,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = Spring.StiffnessLow
        ),
        label = "escalaLogo"
    )

    val opacidadLogo by animateFloatAsState(
        targetValue = if (iniciado) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "opacidadLogo"
    )

    val transicion = rememberInfiniteTransition(label = "bucle")

    // Anillo exterior girando
    val anguloAnillo by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing)
        ),
        label = "anguloAnillo"
    )

    // Anillo interior girando al reves, mas lento
    val anguloAnillo2 by transicion.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = LinearEasing)
        ),
        label = "anguloAnillo2"
    )

    // Halo pulsante
    val pulso by transicion.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulso"
    )

    // Barra de progreso: 2 segundos exactos
    val progreso by animateFloatAsState(
        targetValue = if (iniciado) 1f else 0f,
        animationSpec = tween(1900, easing = LinearEasing),
        label = "progreso"
    )

    LaunchedEffect(Unit) {
        iniciado = true
        delay(2200)
        onTerminado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(KotrilSuperficie, KotrilNegro),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {

            Box(contentAlignment = Alignment.Center) {

                // Halo pulsante detras del logo
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .scale(pulso)
                        .alpha(opacidadLogo * 0.5f)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                KotrilCian.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2f
                        ),
                        radius = size.minDimension / 2f
                    )
                }

                // Anillos giratorios
                Canvas(
                    modifier = Modifier
                        .size(255.dp)
                        .alpha(opacidadLogo)
                ) {
                    val radio = size.minDimension / 2f - 6f
                    val esquina = Offset(center.x - radio, center.y - radio)
                    val tam = Size(radio * 2, radio * 2)

                    // Arcos exteriores
                    for (i in 0 until 3) {
                        rotate(anguloAnillo + i * 120f, center) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(KotrilCian, KotrilCianClaro, KotrilCian)
                                ),
                                startAngle = 0f,
                                sweepAngle = 46f,
                                useCenter = false,
                                topLeft = esquina,
                                size = tam,
                                style = Stroke(width = 4f)
                            )
                        }
                    }

                    // Arcos interiores, giro inverso
                    val radio2 = radio * 0.82f
                    val esquina2 = Offset(center.x - radio2, center.y - radio2)
                    val tam2 = Size(radio2 * 2, radio2 * 2)

                    for (i in 0 until 2) {
                        rotate(anguloAnillo2 + i * 180f, center) {
                            drawArc(
                                color = KotrilAzulElec.copy(alpha = 0.65f),
                                startAngle = 0f,
                                sweepAngle = 70f,
                                useCenter = false,
                                topLeft = esquina2,
                                size = tam2,
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }

                // El logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Kotril",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(190.dp)
                        .scale(escalaLogo)
                        .alpha(opacidadLogo)
                        .clip(RoundedCornerShape(32.dp))
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = "KOTRIL",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 10.sp,
                color = KotrilTextoAlto,
                modifier = Modifier.alpha(opacidadLogo)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "TELEMETRIA OMNIDIRECCIONAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = KotrilCian,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(opacidadLogo * 0.9f)
            )

            Spacer(Modifier.height(52.dp))

            // Barra de progreso
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KotrilBorde)
                    .alpha(opacidadLogo)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(KotrilCianOscuro, KotrilCianClaro)
                            )
                        )
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = when {
                    progreso < 0.35f -> "INICIANDO SISTEMAS"
                    progreso < 0.7f  -> "CARGANDO TELEMETRIA"
                    else             -> "ESTABLECIENDO ENLACE"
                },
                fontSize = 9.sp,
                letterSpacing = 2.5.sp,
                color = KotrilTextoBajo,
                modifier = Modifier.alpha(opacidadLogo)
            )
        }

        Text(
            text = "FRACTAL-BOTS",
            fontSize = 9.sp,
            letterSpacing = 3.sp,
            color = KotrilTextoBajo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(opacidadLogo * 0.7f)
        )
    }
}


