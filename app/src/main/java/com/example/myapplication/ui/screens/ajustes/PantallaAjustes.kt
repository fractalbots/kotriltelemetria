package com.example.myapplication.ui.screens.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun PantallaAjustes(
    viewModel: AjustesViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferenciasState.collectAsStateWithLifecycle()
    val prueba by viewModel.estadoPrueba.collectAsStateWithLifecycle()

    // Campo local: se sincroniza cuando llega la IP guardada, pero
    // mientras el usuario escribe no se le pisa lo que teclea.
    var ipTexto by remember(prefs.ipRobot) { mutableStateOf(prefs.ipRobot) }

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Seccion("ENLACE CON EL ROBOT") {
            OutlinedTextField(
                value = ipTexto,
                onValueChange = { ipTexto = it },
                label = { Text("Direccion IP") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "El robot tiene IP fija 192.168.1.200. Solo cambia si lo " +
                "mueves a otra red.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { viewModel.guardarIp(ipTexto) },
                    enabled = ipTexto.isNotBlank() && ipTexto != prefs.ipRobot,
                    shape = RoundedCornerShape(11.dp)
                ) { Text("GUARDAR", fontSize = 12.sp, letterSpacing = 1.sp) }

                OutlinedButton(
                    onClick = { viewModel.probarConexion() },
                    enabled = prueba !is EstadoPrueba.Probando,
                    shape = RoundedCornerShape(11.dp)
                ) { Text("PROBAR", fontSize = 12.sp, letterSpacing = 1.sp) }
            }

            Spacer(Modifier.height(12.dp))

            when (val p = prueba) {
                is EstadoPrueba.Inactivo -> Unit
                is EstadoPrueba.Probando -> Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Probando enlace...", style = MaterialTheme.typography.bodySmall)
                }
                is EstadoPrueba.Exito -> ResultadoPrueba(
                    p.mensaje, KotrilVerde, true
                )
                is EstadoPrueba.Fallo -> ResultadoPrueba(
                    p.mensaje, KotrilRojo, false
                )
            }
        }

        Seccion("CONDUCCION") {
            EtiquetaValor(
                "Velocidad maxima",
                "${(prefs.velocidadMaxima * 100).roundToInt()}%"
            )
            Slider(
                value = prefs.velocidadMaxima,
                onValueChange = { viewModel.guardarVelocidad(it) },
                valueRange = 0.1f..1f,
                steps = 8
            )
            Text(
                "Limita la potencia enviada a los motores. Util para pruebas " +
                "en espacios reducidos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Seccion("ALERTAS") {
            EtiquetaValor("Umbral de gas", "${prefs.umbralGas} / 100")
            Slider(
                value = prefs.umbralGas.toFloat(),
                onValueChange = { viewModel.guardarUmbralGas(it.roundToInt()) },
                valueRange = 0f..100f
            )
            Text(
                "Al superarse, la pantalla de telemetria muestra una alerta. " +
                "El MQ-135 no entrega ppm calibrados, por eso se usa un " +
                "indice relativo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Seccion("APARIENCIA Y UNIDADES") {
            FilaSwitch(
                "Modo oscuro",
                "Tema oscuro tipo panel de instrumentos",
                prefs.modoOscuro
            ) { viewModel.guardarModoOscuro(it) }

            HorizontalDivider(Modifier.padding(vertical = 6.dp))

            FilaSwitch(
                "Temperatura en Fahrenheit",
                "Afecta a la pantalla de telemetria",
                prefs.usarFahrenheit
            ) { viewModel.guardarFahrenheit(it) }
        }

        Spacer(Modifier.height(10.dp))

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "KOTRIL",
                style = MaterialTheme.typography.labelLarge,
                color = KotrilCian
            )
            Text(
                "Telemetria omnidireccional  ·  v1.0",
                style = MaterialTheme.typography.labelSmall,
                color = KotrilTextoBajo
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "FRACTAL-BOTS  ·  Quito, Ecuador",
                style = MaterialTheme.typography.labelSmall,
                color = KotrilTextoBajo
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))
            contenido()
        }
    }
}

@Composable
private fun EtiquetaValor(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun FilaSwitch(
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}

@Composable
private fun ResultadoPrueba(
    mensaje: String,
    color: androidx.compose.ui.graphics.Color,
    exito: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (exito) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(mensaje, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
