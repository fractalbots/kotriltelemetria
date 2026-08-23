package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
/** Las ocho direcciones que puede tomar un robot mecanum. */
enum class Direccion(val vx: Float, val vy: Float, val etiqueta: String) {
    ADELANTE       ( 1f,  0f, "N"),
    ADELANTE_DER   ( 1f,  1f, "NE"),
    DERECHA        ( 0f,  1f, "E"),
    ATRAS_DER      (-1f,  1f, "SE"),
    ATRAS          (-1f,  0f, "S"),
    ATRAS_IZQ      (-1f, -1f, "SO"),
    IZQUIERDA      ( 0f, -1f, "O"),
    ADELANTE_IZQ   ( 1f, -1f, "NO")
}

/**
 * Pad direccional de 8 direcciones.
 *
 * Alternativa al joystick analogico: en lugar de dosificar la
 * velocidad con el dedo, cada boton manda una direccion fija a
 * la velocidad maxima configurada.
 *
 * Es mas preciso para pruebas repetibles — util cuando quieres
 * que el robot avance exactamente recto para medir algo — y mas
 * comodo si el usuario prefiere pulsar a arrastrar.
 *
 * Las diagonales solo tienen sentido en un robot mecanum: un
 * robot diferencial no puede desplazarse en diagonal.
 */
@Composable
fun PadDireccional(
    modifier: Modifier = Modifier,
    tamano: Dp = 230.dp,
    habilitado: Boolean = true,
    hapticaActiva: Boolean = true,
    onDireccion: (vx: Float, vy: Float) -> Unit
) {
    var activa by remember { mutableStateOf<Direccion?>(null) }
    val haptica = LocalHapticFeedback.current

    Box(
        modifier = modifier.size(tamano),
        contentAlignment = Alignment.Center
    ) {
        // Fondo circular decorativo
        Canvas(Modifier.size(tamano)) {
            val r = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(KotrilSuperficie2, KotrilNegro),
                    center = center,
                    radius = r
                ),
                radius = r * 0.98f,
                center = center
            )
            drawCircle(
                color = KotrilBorde,
                radius = r * 0.98f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotonDir(Direccion.ADELANTE_IZQ, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
                BotonDir(Direccion.ADELANTE, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
                BotonDir(Direccion.ADELANTE_DER, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotonDir(Direccion.IZQUIERDA, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
                CentroPad()
                BotonDir(Direccion.DERECHA, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotonDir(Direccion.ATRAS_IZQ, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
                BotonDir(Direccion.ATRAS, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
                BotonDir(Direccion.ATRAS_DER, activa, habilitado, hapticaActiva,
                    haptica, { activa = it }, onDireccion)
            }
        }
    }
}

@Composable
private fun BotonDir(
    direccion: Direccion,
    activa: Direccion?,
    habilitado: Boolean,
    hapticaActiva: Boolean,
    haptica: androidx.compose.ui.hapticfeedback.HapticFeedback,
    setActiva: (Direccion?) -> Unit,
    onDireccion: (Float, Float) -> Unit
) {
    val presionada = activa == direccion
    val escala by animateFloatAsState(
        targetValue = if (presionada) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "escalaDir"
    )
    val esDiagonal = direccion.vx != 0f && direccion.vy != 0f

    Box(
        modifier = Modifier
            .size(58.dp)
            .scale(escala)
            .pointerInput(habilitado) {
                if (!habilitado) return@pointerInput
                detectTapGestures(
                    onPress = {
                        setActiva(direccion)
                        if (hapticaActiva) {
                            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        // Se mantiene mientras el dedo este abajo
                        onDireccion(direccion.vx, direccion.vy)
                        tryAwaitRelease()
                        setActiva(null)
                        onDireccion(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val color = when {
                !habilitado -> KotrilTextoBajo.copy(alpha = 0.3f)
                presionada -> KotrilCianClaro
                esDiagonal -> KotrilAzulElec
                else -> KotrilCian
            }

            if (presionada) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(color.copy(alpha = 0.5f), Color.Transparent),
                        center = center, radius = r * 1.5f
                    ),
                    radius = r * 1.3f, center = center
                )
            }

            drawCircle(
                brush = Brush.linearGradient(
                    colors = if (presionada)
                        listOf(color.copy(alpha = 0.45f), color.copy(alpha = 0.2f))
                    else listOf(KotrilSuperficie2, KotrilSuperficie)
                ),
                radius = r * 0.88f,
                center = center
            )
            drawCircle(
                color = color.copy(alpha = if (presionada) 1f else 0.55f),
                radius = r * 0.88f,
                center = center,
                style = Stroke(width = if (presionada) 2.5f else 1.5f)
            )
            // Flecha triangular apuntando en la direccion.
            // Se calculan los vertices con seno y coseno en vez de
            // rotar el lienzo: menos dependencias y mismo resultado.
            val rad = kotlin.math.atan2(
                direccion.vy.toDouble(), -direccion.vx.toDouble()
            ).toFloat()

            val dirX = kotlin.math.sin(rad)      // hacia donde apunta
            val dirY = -kotlin.math.cos(rad)
            val perpX = -dirY                    // perpendicular
            val perpY = dirX

            val punta = r * 0.42f
            val ancho = r * 0.30f

            fun punto(avance: Float, lado: Float) = Offset(
                center.x + dirX * avance + perpX * lado,
                center.y + dirY * avance + perpY * lado
            )

            val ruta = Path().apply {
                moveTo(punto(punta, 0f).x, punto(punta, 0f).y)
                lineTo(punto(-punta * 0.55f, -ancho).x, punto(-punta * 0.55f, -ancho).y)
                lineTo(punto(-punta * 0.18f, 0f).x, punto(-punta * 0.18f, 0f).y)
                lineTo(punto(-punta * 0.55f, ancho).x, punto(-punta * 0.55f, ancho).y)
                close()
            }
            drawPath(ruta, color)
        }
    }
}

@Composable
private fun CentroPad() {
    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(
                color = KotrilNegro.copy(alpha = 0.6f),
                radius = r * 0.7f,
                center = center
            )
            drawCircle(
                color = KotrilBorde,
                radius = r * 0.7f,
                center = center,
                style = Stroke(width = 1.2f)
            )
        }
        Text(
            "8D",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = KotrilTextoBajo
        )
    }
}

/**
 * Par de botones de rotacion, para el modo pad.
 */
@Composable
fun BotonesRotacion(
    habilitado: Boolean = true,
    hapticaActiva: Boolean = true,
    onRotacion: (Float) -> Unit
) {
    val haptica = LocalHapticFeedback.current
    var activo by remember { mutableStateOf(0) }

    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        BotonGiro("GIRO IZQ", -1f, activo == -1, habilitado, hapticaActiva,
            haptica, { activo = it }, onRotacion)
        BotonGiro("GIRO DER", 1f, activo == 1, habilitado, hapticaActiva,
            haptica, { activo = it }, onRotacion)
    }
}

@Composable
private fun BotonGiro(
    etiqueta: String,
    valor: Float,
    presionado: Boolean,
    habilitado: Boolean,
    hapticaActiva: Boolean,
    haptica: androidx.compose.ui.hapticfeedback.HapticFeedback,
    setActivo: (Int) -> Unit,
    onRotacion: (Float) -> Unit
) {
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "escalaGiro"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(escala)
                .pointerInput(habilitado) {
                    if (!habilitado) return@pointerInput
                    detectTapGestures(
                        onPress = {
                            setActivo(valor.toInt())
                            if (hapticaActiva) {
                                haptica.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onRotacion(valor)
                            tryAwaitRelease()
                            setActivo(0)
                            onRotacion(0f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f
                val color = if (!habilitado) KotrilTextoBajo.copy(alpha = 0.3f)
                            else if (presionado) KotrilCianClaro else KotrilAzulElec

                if (presionado) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(color.copy(alpha = 0.45f), Color.Transparent),
                            center = center, radius = r * 1.5f
                        ),
                        radius = r * 1.3f, center = center
                    )
                }

                drawCircle(
                    brush = Brush.linearGradient(
                        if (presionado)
                            listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.15f))
                        else listOf(KotrilSuperficie2, KotrilSuperficie)
                    ),
                    radius = r * 0.9f, center = center
                )
                drawCircle(
                    color = color.copy(alpha = if (presionado) 1f else 0.55f),
                    radius = r * 0.9f, center = center,
                    style = Stroke(width = if (presionado) 2.5f else 1.5f)
                )

                // Arco con punta de flecha
                val inicio = if (valor > 0) -50f else 230f
                val barrido = if (valor > 0) 250f else -250f
                drawArc(
                    color = color,
                    startAngle = inicio,
                    sweepAngle = barrido,
                    useCenter = false,
                    topLeft = Offset(center.x - r * 0.52f, center.y - r * 0.52f),
                    size = androidx.compose.ui.geometry.Size(r * 1.04f, r * 1.04f),
                    style = Stroke(width = 3.5f)
                )

                val angPunta = Math.toRadians((inicio + barrido).toDouble())
                val px = center.x + kotlin.math.cos(angPunta).toFloat() * r * 0.52f
                val py = center.y + kotlin.math.sin(angPunta).toFloat() * r * 0.52f
                drawCircle(color, radius = 5f, center = Offset(px, py))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            etiqueta,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
