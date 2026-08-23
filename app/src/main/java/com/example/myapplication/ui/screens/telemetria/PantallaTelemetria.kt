package com.example.myapplication.ui.screens.telemetria

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.domain.model.Preferencias
import com.example.myapplication.domain.model.Telemetria
import com.example.myapplication.ui.theme.*
import kotlin.math.abs
import androidx.compose.ui.graphics.drawscope.rotate as rotarCanvas
@Composable
fun PantallaTelemetria(
    viewModel: TelemetriaViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs by viewModel.preferencias.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // El compilador obliga a cubrir los tres estados
        when (val e = estado) {
            is TelemetriaUiState.Cargando ->
                VistaCargando(Modifier.align(Alignment.Center))
            is TelemetriaUiState.Error ->
                VistaError(e, { viewModel.reintentar() }, Modifier.align(Alignment.Center))
            is TelemetriaUiState.Exito ->
                VistaDatos(e.datos, prefs) { viewModel.reiniciarOrientacion() }
        }
    }
}

/* ═══════════ ESTADO 1: CARGANDO ═══════════ */
@Composable
private fun VistaCargando(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "carga")
    val angulo by t.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "angulo"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(64.dp).rotate(angulo)) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, KotrilCian, KotrilCianClaro)
                ),
                startAngle = 0f, sweepAngle = 300f, useCenter = false,
                style = Stroke(width = 5f)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "ESTABLECIENDO ENLACE",
            style = MaterialTheme.typography.labelMedium,
            color = KotrilTextoMedio
        )
    }
}

/* ═══════════ ESTADO 2: ERROR ═══════════ */
@Composable
private fun VistaError(
    estado: TelemetriaUiState.Error,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(KotrilRojo.copy(alpha = 0.14f))
                .border(1.5.dp, KotrilRojo.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = KotrilRojo,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "ENLACE PERDIDO",
            style = MaterialTheme.typography.labelLarge,
            color = KotrilRojo
        )
        Spacer(Modifier.height(10.dp))
        Text(
            estado.tipo.mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        estado.detalle?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = KotrilTextoBajo
            )
        }
        Spacer(Modifier.height(26.dp))
        Button(
            onClick = onReintentar,
            shape = RoundedCornerShape(12.dp)
        ) { Text("REINTENTAR") }
    }
}

/* ═══════════ ESTADO 3: DATOS ═══════════ */
@Composable
private fun VistaDatos(
    datos: Telemetria,
    prefs: Preferencias,
    onReiniciarYaw: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        if (!datos.sensorOk) {
            item { Aviso("SENSOR MPU6050 SIN RESPUESTA", KotrilAmbar) }
        }
        if (datos.paroActivo) {
            item { Aviso("PARO DE EMERGENCIA ACTIVO", KotrilRojo) }
        }

        // Alerta de gas: aqui SI se usa el umbral configurado
        if (datos.indiceGas >= prefs.umbralGas) {
            item {
                Aviso(
                    "GAS SOBRE EL UMBRAL (${datos.indiceGas} / ${prefs.umbralGas})",
                    KotrilRojo
                )
            }
        }

        item { HorizonteArtificial(datos) }
        item { PanelAngulos(datos, onReiniciarYaw) }
        item { PanelSensores(datos, prefs) }
        item { PanelMotores(datos) }
        item { PanelEnlace(datos) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

/* ─────── Horizonte artificial ─────── */
@Composable
private fun HorizonteArtificial(datos: Telemetria) {

    val pitchAnim by animateFloatAsState(datos.pitch, spring(), label = "pitch")
    val rollAnim by animateFloatAsState(datos.roll, spring(), label = "roll")

    val peligro = datos.inclinacionTotal > UMBRAL_INCLINACION
    val colorBorde by animateColorAsState(
        if (peligro) KotrilRojo else KotrilCian.copy(alpha = 0.4f),
        label = "borde"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ACTITUD",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(178.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorBorde, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Cielo y tierra girando con el roll y subiendo con el pitch
                Canvas(Modifier.fillMaxSize()) {
                    val h = size.height
                    val desplazamiento = (pitchAnim / 90f) * h * 0.5f

                    rotarCanvas(-rollAnim, center) {                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF1B4A6B), Color(0xFF2B7BA8))
                            ),
                            topLeft = Offset(-size.width, -size.height + desplazamiento),
                            size = androidx.compose.ui.geometry.Size(
                                size.width * 3, size.height * 1.5f
                            )
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF3E2E1B), Color(0xFF1F1710))
                            ),
                            topLeft = Offset(-size.width, size.height * 0.5f + desplazamiento),
                            size = androidx.compose.ui.geometry.Size(
                                size.width * 3, size.height * 1.5f
                            )
                        )
                        // Linea de horizonte
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(-size.width, size.height * 0.5f + desplazamiento),
                            end = Offset(size.width * 2, size.height * 0.5f + desplazamiento),
                            strokeWidth = 2.5f
                        )
                    }

                    // Referencia fija del robot
                    val cy = size.height / 2
                    val cx = size.width / 2
                    drawLine(KotrilAmbar, Offset(cx - 42, cy), Offset(cx - 14, cy), 3.5f)
                    drawLine(KotrilAmbar, Offset(cx + 14, cy), Offset(cx + 42, cy), 3.5f)
                    drawCircle(KotrilAmbar, 3.5f, Offset(cx, cy))
                }
            }

            if (peligro) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "INCLINACION ELEVADA — RIESGO DE VOLCADURA",
                    style = MaterialTheme.typography.labelSmall,
                    color = KotrilRojo
                )
            }
        }
    }
}

/* ─────── Angulos ─────── */
@Composable
private fun PanelAngulos(datos: Telemetria, onReiniciarYaw: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ORIENTACION",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onReiniciarYaw) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("CERO", fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Angulo("PITCH", datos.pitch)
                Angulo("ROLL", datos.roll)
                Angulo("YAW", datos.yaw)
            }
        }
    }
}

@Composable
private fun Angulo(etiqueta: String, valor: Float) {
    val anim by animateFloatAsState(valor, spring(), label = etiqueta)
    val peligro = abs(valor) > UMBRAL_INCLINACION

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = String.format("%+.1f", anim),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,   // sin temblor de digitos
            color = if (peligro) KotrilRojo else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "grados",
            style = MaterialTheme.typography.labelSmall,
            color = KotrilTextoBajo
        )
    }
}

/* ─────── Sensores ─────── */
@Composable
private fun PanelSensores(datos: Telemetria, prefs: Preferencias) {

    // Aqui SI se usa la preferencia de unidad
    val temperatura = if (prefs.usarFahrenheit)
        datos.temperaturaSensor * 9f / 5f + 32f
    else datos.temperaturaSensor
    val unidad = if (prefs.usarFahrenheit) "F" else "C"

    val sobreUmbral = datos.indiceGas >= prefs.umbralGas

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "SENSORES",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))

            Fila(
                "Temp. interna del sensor",
                String.format("%.1f %s", temperatura, unidad)
            )

            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth()) {
                Text(
                    "Indice de calidad del aire",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${datos.indiceGas} / 100",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (sobreUmbral) KotrilRojo
                            else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((datos.indiceGas / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (sobreUmbral) listOf(KotrilAmbar, KotrilRojo)
                                else listOf(KotrilVerde, KotrilCian)
                            )
                        )
                )
                // Marca del umbral configurado por el usuario
                Box(
                    Modifier
                        .fillMaxWidth(prefs.umbralGas / 100f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(KotrilTextoAlto)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Umbral configurado: ${prefs.umbralGas}",
                style = MaterialTheme.typography.labelSmall,
                color = KotrilTextoBajo
            )
        }
    }
}

/* ─────── Motores ─────── */
@Composable
private fun PanelMotores(datos: Telemetria) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "MOTORES  (PWM -255 a 255)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Motor("FL", datos.motores.frontalIzq, Modifier.weight(1f))
                Motor("FR", datos.motores.frontalDer, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Motor("RL", datos.motores.traseroIzq, Modifier.weight(1f))
                Motor("RR", datos.motores.traseroDer, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Motor(etiqueta: String, valor: Int, modifier: Modifier = Modifier) {
    val activo = valor != 0
    val color = when {
        valor > 0 -> KotrilVerde
        valor < 0 -> KotrilAmbar
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .border(
                1.dp,
                if (activo) color.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = KotrilTextoBajo)
        Spacer(Modifier.height(3.dp))
        Text(
            "$valor",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth((abs(valor) / 255f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

/* ─────── Enlace ─────── */
@Composable
private fun PanelEnlace(datos: Telemetria) {
    val calidad = when {
        datos.calidadSenal > -50 -> "EXCELENTE" to KotrilVerde
        datos.calidadSenal > -70 -> "BUENA" to KotrilCian
        datos.calidadSenal > -85 -> "REGULAR" to KotrilAmbar
        else -> "DEBIL" to KotrilRojo
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "ENLACE WIFI",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${datos.calidadSenal} dBm",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                calidad.first,
                style = MaterialTheme.typography.labelLarge,
                color = calidad.second
            )
        }
    }
}

/* ─────── Aviso reutilizable ─────── */
@Composable
private fun Aviso(texto: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

private const val UMBRAL_INCLINACION = 25f
