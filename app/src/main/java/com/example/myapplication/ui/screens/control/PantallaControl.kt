package com.example.myapplication.ui.screens.control

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlin.math.abs

@Composable
fun PantallaControl(
    viewModel: ControlViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(6.dp))

        BarraEstado(estado)

        Spacer(Modifier.height(10.dp))

        ConmutadorModo(
            modoJoystick = estado.modoJoystick,
            onAlternar = { viewModel.alternarModoControl() }
        )

        Spacer(Modifier.height(10.dp))

        PanelMedidores(estado)

        Spacer(Modifier.height(18.dp))

        // ── Controles, con transicion entre modos ──
        AnimatedContent(
            targetState = estado.modoJoystick,
            transitionSpec = {
                (fadeIn(tween(280)) + scaleIn(initialScale = 0.90f, animationSpec = tween(280)))
                    .togetherWith(fadeOut(tween(180)) + scaleOut(targetScale = 0.94f))
            },
            label = "modoControl"
        ) { esJoystick ->

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                if (esJoystick) {
                    Etiqueta("TRASLACION", "arrastra para dosificar la velocidad")
                    Spacer(Modifier.height(8.dp))
                    JoystickPro(
                        tamano = 208.dp,
                        habilitado = !estado.paroActivo,
                        colorAcento = KotrilCian,
                        onCambio = { x, y -> viewModel.onTraslacion(x, y) }
                    )
                    Spacer(Modifier.height(18.dp))
                    Etiqueta("ROTACION", "giro sobre el propio eje")
                    Spacer(Modifier.height(8.dp))
                    JoystickPro(
                        tamano = 138.dp,
                        soloHorizontal = true,
                        habilitado = !estado.paroActivo,
                        colorAcento = KotrilAzulElec,
                        onCambio = { x, _ -> viewModel.onRotacion(x) }
                    )
                } else {
                    Etiqueta("PAD 8 DIRECCIONES", "las diagonales solo existen en mecanum")
                    Spacer(Modifier.height(8.dp))
                    PadDireccional(
                        tamano = 218.dp,
                        habilitado = !estado.paroActivo,
                        hapticaActiva = estado.hapticaActiva,
                        onDireccion = { vx, vy -> viewModel.onDireccionPad(vx, vy) }
                    )
                    Spacer(Modifier.height(18.dp))
                    Etiqueta("ROTACION", "mantén pulsado para girar")
                    Spacer(Modifier.height(8.dp))
                    BotonesRotacion(
                        habilitado = !estado.paroActivo,
                        hapticaActiva = estado.hapticaActiva,
                        onRotacion = { viewModel.onRotacion(it) }
                    )
                }
            }
        }

        Spacer(Modifier.height(26.dp))

        BotonParoIndustrial(
            activo = estado.paroActivo,
            hapticaActiva = estado.hapticaActiva,
            onClick = { viewModel.alternarParo() }
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = if (estado.paroActivo)
                "Pulsa de nuevo para liberar el paro"
            else "Detiene el robot al instante",
            style = MaterialTheme.typography.labelSmall,
            color = if (estado.paroActivo) KotrilRojo
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
    }
}

/* ─────────── Conmutador joystick / pad ─────────── */
@Composable
private fun ConmutadorModo(modoJoystick: Boolean, onAlternar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        OpcionModo(
            texto = "JOYSTICK",
            icono = Icons.Filled.Gamepad,
            seleccionado = modoJoystick,
            modifier = Modifier.weight(1f)
        ) { if (!modoJoystick) onAlternar() }

        OpcionModo(
            texto = "BOTONES",
            icono = Icons.Filled.GridView,
            seleccionado = !modoJoystick,
            modifier = Modifier.weight(1f)
        ) { if (modoJoystick) onAlternar() }
    }
}

@Composable
private fun OpcionModo(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fondo by animateColorAsState(
        if (seleccionado) KotrilCian else Color.Transparent,
        label = "fondoModo"
    )
    val contenido by animateColorAsState(
        if (seleccionado) KotrilNegro else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contenidoModo"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(fondo)
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = contenido,
            modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            color = contenido
        )
    }
}

/* ─────────── Barra de estado ─────────── */
@Composable
private fun BarraEstado(estado: ControlUiState) {
    val t = rememberInfiniteTransition(label = "latido")
    val alfa by t.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alfa"
    )
    val color = if (estado.conectado) KotrilVerde else KotrilRojo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (estado.conectado) alfa else 1f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            if (estado.conectado) "ENLACE ACTIVO" else "SIN ENLACE",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        Text(
            "VEL ${(estado.velocidadMaxima * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ─────────── Medidores ─────────── */
@Composable
private fun PanelMedidores(estado: ControlUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Medidor("VX", estado.comando.vx, KotrilCian)
        Separador()
        Medidor("VY", estado.comando.vy, KotrilCian)
        Separador()
        Medidor("W", estado.comando.w, KotrilAzulElec)
    }
}

@Composable
private fun Medidor(etiqueta: String, valor: Float, color: Color) {
    val activo = abs(valor) > 0.02f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(3.dp))
        Text(
            String.format("%+.2f", valor),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,   // sin temblor de digitos
            color = if (activo) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .width(42.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(abs(valor).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun Separador() {
    Box(
        Modifier
            .width(1.dp)
            .height(42.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@Composable
private fun Etiqueta(titulo: String, descripcion: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
