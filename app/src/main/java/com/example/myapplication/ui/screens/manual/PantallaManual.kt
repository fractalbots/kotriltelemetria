package com.example.myapplication.ui.screens.manual

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

private data class SeccionManual(
    val icono: ImageVector,
    val titulo: String,
    val resumen: String,
    val pasos: List<String> = emptyList(),
    val notas: List<String> = emptyList(),
    val color: Color = KotrilCian
)

/**
 * Manual de funcionamiento integrado.
 *
 * Un manual dentro de la app evita que el usuario dependa de un
 * PDF aparte. Las secciones se despliegan una a una para no
 * abrumar con un muro de texto.
 */
@Composable
fun PantallaManual(modifier: Modifier = Modifier) {

    val secciones = remember { construirSecciones() }
    var abierta by remember { mutableStateOf<Int?>(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item { Portada() }

        itemsIndexed(secciones) { indice, seccion ->
            TarjetaSeccion(
                seccion = seccion,
                expandida = abierta == indice,
                onClick = { abierta = if (abierta == indice) null else indice }
            )
        }

        item { PieDePagina() }
    }
}

/* Helper: LazyColumn no trae itemsIndexed para listas simples
   en todas las versiones, asi que lo definimos explicito. */
private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexed(
    lista: List<SeccionManual>,
    contenido: @Composable (Int, SeccionManual) -> Unit
) {
    items(lista.size) { i -> contenido(i, lista[i]) }
}

@Composable
private fun Portada() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        KotrilCianOscuro.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .border(
                1.dp,
                KotrilCian.copy(alpha = 0.3f),
                RoundedCornerShape(18.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            "MANUAL DE OPERACION",
            style = MaterialTheme.typography.labelLarge,
            color = KotrilCian
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Todo lo que necesitas para operar el robot: conexion, " +
            "controles, registro de ensayos y solucion de problemas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(KotrilAmbar.copy(alpha = 0.12f))
                .border(1.dp, KotrilAmbar.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = KotrilAmbar,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Primera prueba SIEMPRE con el robot elevado sobre una " +
                "caja, con las ruedas al aire.",
                style = MaterialTheme.typography.bodySmall,
                color = KotrilAmbar
            )
        }
    }
}

@Composable
private fun TarjetaSeccion(
    seccion: SeccionManual,
    expandida: Boolean,
    onClick: () -> Unit
) {
    val giro by animateFloatAsState(
        targetValue = if (expandida) 180f else 0f,
        label = "giroFlecha"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(seccion.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        seccion.icono,
                        contentDescription = null,
                        tint = seccion.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        seccion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        seccion.resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = if (expandida) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(giro)
                )
            }

            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 18.dp)) {

                    HorizontalDivider(Modifier.padding(bottom = 14.dp))

                    seccion.pasos.forEachIndexed { i, paso ->
                        Row(Modifier.padding(bottom = 12.dp)) {
                            Box(
                                Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(seccion.color.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${i + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = seccion.color
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                paso,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    seccion.notas.forEach { nota ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                "·",
                                color = seccion.color,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                nota,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieDePagina() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "KOTRIL",
            style = MaterialTheme.typography.labelLarge,
            color = KotrilCian
        )
        Text(
            "Telemetria omnidireccional · v1.0",
            style = MaterialTheme.typography.labelSmall,
            color = KotrilTextoBajo
        )
        Spacer(Modifier.height(3.dp))
        Text(
            "FRACTAL-BOTS · Quito, Ecuador",
            style = MaterialTheme.typography.labelSmall,
            color = KotrilTextoBajo
        )
    }
}

/* ═════════ Contenido del manual ═════════ */
private fun construirSecciones() = listOf(

    SeccionManual(
        icono = Icons.Filled.Wifi,
        titulo = "Conectar el robot",
        resumen = "Antes de conducir",
        color = KotrilCian,
        pasos = listOf(
            "Enciende el robot y espera unos 10 segundos: primero " +
            "calibra el giroscopio y luego se conecta al WiFi.",
            "Conecta el celular a la MISMA red WiFi que el robot.",
            "Ve a Ajustes y pulsa PROBAR. Debe responder " +
            "\"Robot conectado y sensor operativo\".",
            "Si falla, revisa que la IP sea 192.168.1.200."
        ),
        notas = listOf(
            "Durante la calibracion el robot debe estar completamente " +
            "quieto, o los angulos quedaran desviados.",
            "La IP es fija: no cambia aunque se reinicie el router."
        )
    ),

    SeccionManual(
        icono = Icons.Filled.Gamepad,
        titulo = "Conducir: joystick o botones",
        resumen = "Dos modos de control",
        color = KotrilAzulElec,
        pasos = listOf(
            "En la pantalla Control, el conmutador de arriba cambia " +
            "entre JOYSTICK y BOTONES.",
            "JOYSTICK: arrastra el dedo para dosificar la velocidad. " +
            "Cuanto mas lejos del centro, mas rapido.",
            "BOTONES: pad de 8 direcciones a velocidad fija. Mas " +
            "preciso para pruebas repetibles.",
            "El joystick de abajo (o los dos botones circulares) " +
            "hacen girar el robot sobre su propio eje.",
            "Al soltar, el robot frena de inmediato."
        ),
        notas = listOf(
            "Las diagonales solo funcionan porque las ruedas son " +
            "mecanum: un robot normal no puede desplazarse de lado.",
            "La velocidad maxima se limita desde Ajustes. Para " +
            "espacios pequenos, bajala al 30%.",
            "La app envia comandos 10 veces por segundo. El robot " +
            "frena solo si pasan 500 ms sin recibir nada."
        )
    ),

    SeccionManual(
        icono = Icons.Filled.Dangerous,
        titulo = "Paro de emergencia",
        resumen = "El boton rojo",
        color = KotrilRojo,
        pasos = listOf(
            "Pulsa el boton rojo: el robot se detiene al instante y " +
            "los controles quedan bloqueados.",
            "El boton late en rojo mientras el paro esta activo.",
            "Pulsalo de nuevo para liberar y volver a conducir."
        ),
        notas = listOf(
            "El paro actua en los dos extremos: la app deja de enviar " +
            "comandos y el firmware corta los motores."
        )
    ),

    SeccionManual(
        icono = Icons.Filled.ShowChart,
        titulo = "Leer la telemetria",
        resumen = "Que significa cada dato",
        color = KotrilVerde,
        pasos = listOf(
            "El horizonte artificial gira con el roll y sube o baja " +
            "con el pitch, igual que en un avion.",
            "PITCH: inclinacion adelante/atras. ROLL: inclinacion " +
            "lateral. YAW: rumbo.",
            "Pulsa CERO para reiniciar el yaw a la orientacion actual.",
            "El indice de gas va de 0 a 100. La marca blanca en la " +
            "barra es el umbral que configuraste."
        ),
        notas = listOf(
            "La temperatura es la INTERNA del chip MPU6050, no la del " +
            "ambiente: marca entre 15 y 20 grados de mas.",
            "El yaw deriva lentamente porque no hay magnetometro. Es " +
            "normal: usa el boton CERO cuando haga falta.",
            "Si la inclinacion pasa de 25 grados, los valores se " +
            "ponen en rojo por riesgo de volcadura."
        )
    ),

    SeccionManual(
        icono = Icons.Filled.PhotoCamera,
        titulo = "Registrar un ensayo",
        resumen = "Bitacora con foto y ubicacion",
        color = KotrilAmbar,
        pasos = listOf(
            "Termina la prueba y ve a la pestana ENSAYOS.",
            "Pulsa NUEVO ENSAYO. La app captura automaticamente la " +
            "telemetria del robot en ese momento.",
            "Escribe un titulo y tus observaciones.",
            "TOMAR FOTO: acepta el permiso y fotografia el robot en " +
            "su posicion final, el obstaculo o el dano.",
            "OBTENER UBICACION: guarda las coordenadas GPS.",
            "GUARDAR ENSAYO. Queda en el historial para siempre."
        ),
        notas = listOf(
            "Si niegas los permisos, el ensayo se guarda igual sin " +
            "foto o sin coordenadas.",
            "Las fotos se guardan en el almacenamiento interno de la " +
            "app; la base de datos solo guarda la ruta."
        )
    ),

    SeccionManual(
        icono = Icons.Filled.Build,
        titulo = "Si algo no funciona",
        resumen = "Problemas frecuentes",
        color = KotrilTextoMedio,
        pasos = listOf(
            "SIN ENLACE: revisa que el robot este encendido y que el " +
            "celular este en la misma red WiFi.",
            "SENSOR SIN RESPUESTA: el MPU6050 no arranco. El firmware " +
            "reintenta solo cada 5 segundos.",
            "EL ROBOT NO SE MUEVE: verifica la bateria y que el pin " +
            "STBY de los drivers este en alto.",
            "SE MUEVE RARO: revisa que los rodillos de las ruedas " +
            "formen una X vista desde arriba."
        ),
        notas = listOf(
            "Abre http://192.168.1.200/diagnostico en el navegador: " +
            "el robot dice exactamente que le pasa.",
            "El emulador de Android NO alcanza la red local. Hay que " +
            "usar un celular fisico."
        )
    )
)
