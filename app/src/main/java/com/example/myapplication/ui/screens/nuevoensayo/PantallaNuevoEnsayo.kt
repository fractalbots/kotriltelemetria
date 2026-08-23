package com.example.myapplication.ui.screens.nuevoensayo

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.myapplication.hardware.ArchivoFoto
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNuevoEnsayo(
    viewModel: NuevoEnsayoViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var uriTemporal by remember { mutableStateOf<Uri?>(null) }
    var permisoCamaraDenegado by remember { mutableStateOf(false) }
    var permisoGpsDenegado by remember { mutableStateOf(false) }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onVolver()
    }

    /* Contrato TakePicture: recibe el Uri donde escribir la imagen
       y devuelve true si el usuario confirmo la foto. */
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) uriTemporal?.let { viewModel.onFotoTomada(it.toString()) }
    }

    /* Permiso de camara. Si se niega, se explica y el ensayo se
       puede guardar igual sin foto: la funcionalidad principal
       nunca se bloquea. */
    val permisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            permisoCamaraDenegado = false
            val archivo = ArchivoFoto.crearArchivo(context)
            val uri = ArchivoFoto.uriPara(context, archivo)
            uriTemporal = uri
            lanzadorCamara.launch(uri)
        } else {
            permisoCamaraDenegado = true
        }
    }

    val permisoGps = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        if (resultados.values.any { it }) {
            permisoGpsDenegado = false
            viewModel.obtenerUbicacion()
        } else {
            permisoGpsDenegado = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "KOTRIL",
                            fontSize = 10.sp,
                            letterSpacing = 3.sp,
                            color = KotrilCian
                        )
                        Text("REGISTRAR ENSAYO",
                            style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::onTitulo,
                label = { Text("Titulo del ensayo *") },
                placeholder = { Text("Ej: Prueba de traccion en alfombra") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.notas,
                onValueChange = viewModel::onNotas,
                label = { Text("Observaciones") },
                placeholder = { Text("Que paso, que fallo, que ajustar") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // ── Foto ──
            Seccion("EVIDENCIA VISUAL") {
                Text(
                    "Los sensores dan numeros; la foto muestra el contexto: " +
                    "posicion final, obstaculo o dano.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                estado.uriFoto?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Foto del ensayo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(14.dp))
                }

                Button(
                    onClick = { permisoCamara.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (estado.uriFoto == null) "TOMAR FOTO" else "REPETIR FOTO",
                        fontSize = 12.sp, letterSpacing = 1.sp
                    )
                }

                if (permisoCamaraDenegado) {
                    Spacer(Modifier.height(10.dp))
                    AvisoPermiso(
                        "Permiso de camara denegado. Puedes guardar el ensayo " +
                        "sin foto, o concederlo desde los ajustes del sistema."
                    )
                }
            }

            // ── Ubicacion ──
            Seccion("UBICACION") {
                if (estado.latitud != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(KotrilCian.copy(alpha = 0.10f))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = KotrilCian,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            String.format("%.5f, %.5f", estado.latitud, estado.longitud),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                OutlinedButton(
                    onClick = {
                        permisoGps.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    enabled = !estado.buscandoUbicacion,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (estado.buscandoUbicacion) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("BUSCANDO...", fontSize = 12.sp, letterSpacing = 1.sp)
                    } else {
                        Icon(Icons.Filled.LocationOn, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (estado.latitud == null) "OBTENER UBICACION"
                            else "ACTUALIZAR",
                            fontSize = 12.sp, letterSpacing = 1.sp
                        )
                    }
                }

                if (permisoGpsDenegado) {
                    Spacer(Modifier.height(10.dp))
                    AvisoPermiso(
                        "Permiso de ubicacion denegado. El ensayo se guardara " +
                        "sin coordenadas."
                    )
                }
            }

            // ── Telemetria ──
            Seccion("TELEMETRIA CAPTURADA") {
                if (estado.telemetriaCapturada) {
                    Fila("Pitch", String.format("%+.1f grados", estado.pitch))
                    Fila("Roll", String.format("%+.1f grados", estado.roll))
                    Fila("Inclinacion maxima",
                         String.format("%.1f grados", estado.inclinacionMaxima))
                    Fila("Temp. sensor", String.format("%.1f C", estado.temperatura))
                    Fila("Indice de gas", "${estado.indiceGas} / 100")
                } else {
                    Text(
                        "Sin enlace con el robot. El ensayo se guardara sin " +
                        "datos de telemetria.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KotrilAmbar
                    )
                }
            }

            estado.aviso?.let { AvisoPermiso(it) }

            Button(
                onClick = { viewModel.guardar() },
                enabled = estado.puedeGuardar,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                } else {
                    Text("GUARDAR ENSAYO", fontSize = 13.sp, letterSpacing = 1.2.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
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
            Spacer(Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
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
private fun AvisoPermiso(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(KotrilAmbar.copy(alpha = 0.12f))
            .border(1.dp, KotrilAmbar.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
            .padding(13.dp)
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = KotrilAmbar
        )
    }
}
