package com.example.myapplication.ui.screens.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.myapplication.domain.model.Ensayo
import com.example.myapplication.ui.theme.*

@Composable
fun PantallaHistorial(
    viewModel: HistorialViewModel,
    onNuevoEnsayo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

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

        when (val e = estado) {

            is HistorialUiState.Cargando ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is HistorialUiState.Vacio -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(KotrilCian.copy(alpha = 0.10f))
                        .border(1.dp, KotrilCian.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = KotrilCian,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "SIN ENSAYOS REGISTRADOS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Termina una prueba con el robot y registrala con foto, " +
                    "ubicacion y telemetria.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /* LazyColumn: solo compone los elementos visibles.
               Con muchos ensayos y sus fotos, un Column normal
               cargaria todo de golpe y consumiria memoria de mas. */
            is HistorialUiState.ConDatos -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        "${e.ensayos.size} ENSAYO${if (e.ensayos.size == 1) "" else "S"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(
                    items = e.ensayos,
                    key = { it.id }              // evita recomposiciones de mas
                ) { ensayo ->
                    TarjetaEnsayo(ensayo) { viewModel.eliminar(ensayo) }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevoEnsayo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("NUEVO ENSAYO", fontSize = 12.sp, letterSpacing = 1.sp) },
            containerColor = KotrilCian,
            contentColor = KotrilNegro,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
        )
    }
}

@Composable
private fun TarjetaEnsayo(ensayo: Ensayo, onEliminar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            if (ensayo.tieneFoto) {
                Box {
                    // Coil carga la foto desde el almacenamiento interno
                    AsyncImage(
                        model = ensayo.uriFoto,
                        contentDescription = "Foto del ensayo ${ensayo.titulo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    // Degradado inferior para que el texto respire
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                    )
                }
            }

            Column(Modifier.padding(18.dp)) {

                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            ensayo.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            ensayo.fechaLegible,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = KotrilCian
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar ensayo",
                            tint = KotrilRojo.copy(alpha = 0.8f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                if (ensayo.notas.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        ensayo.notas,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Dato("PITCH", String.format("%+.1f", ensayo.pitch))
                    Dato("ROLL", String.format("%+.1f", ensayo.roll))
                    Dato("INCL", String.format("%.1f", ensayo.inclinacionMaxima))
                    Dato("GAS", "${ensayo.indiceGas}")
                }

                if (ensayo.tieneUbicacion) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = KotrilCian
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            ensayo.ubicacionLegible,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = KotrilTextoBajo
        )
        Spacer(Modifier.height(3.dp))
        Text(
            valor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
