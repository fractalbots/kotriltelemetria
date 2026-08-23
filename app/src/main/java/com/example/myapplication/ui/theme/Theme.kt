package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val EsquemaOscuro = darkColorScheme(
    primary            = KotrilCian,
    onPrimary          = KotrilNegro,
    primaryContainer   = KotrilCianOscuro,
    onPrimaryContainer = KotrilTextoAlto,

    secondary          = KotrilAzulElec,
    onSecondary        = Color.White,

    tertiary           = KotrilVerde,
    onTertiary         = KotrilNegro,

    background         = KotrilFondo,
    onBackground       = KotrilTextoAlto,

    surface            = KotrilSuperficie,
    onSurface          = KotrilTextoAlto,
    surfaceVariant     = KotrilSuperficie2,
    onSurfaceVariant   = KotrilTextoMedio,

    outline            = KotrilBorde,
    outlineVariant     = KotrilBorde,

    error              = KotrilRojo,
    onError            = Color.White,
    errorContainer     = KotrilRojoOscuro,
    onErrorContainer   = Color(0xFFFFD6DD)
)

private val EsquemaClaro = lightColorScheme(
    primary            = KotrilCianOscuro,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFCCEBFF),
    onPrimaryContainer = Color(0xFF002B44),

    secondary          = KotrilAzulElec,
    onSecondary        = Color.White,

    tertiary           = Color(0xFF00875F),
    onTertiary         = Color.White,

    background         = ClaroFondo,
    onBackground       = ClaroTexto,

    surface            = ClaroSuperficie,
    onSurface          = ClaroTexto,
    surfaceVariant     = Color(0xFFE7EEF7),
    onSurfaceVariant   = Color(0xFF44586E),

    outline            = ClaroBorde,

    error              = Color(0xFFC00E2E),
    onError            = Color.White,
    errorContainer     = Color(0xFFFFDAE0),
    onErrorContainer   = Color(0xFF5C0012)
)

/**
 * Tipografia tecnica.
 *
 * Los numeros van en monoespaciada a proposito: con fuente
 * proporcional, un 1 y un 8 tienen anchos distintos y los
 * valores tiemblan al actualizarse 2 veces por segundo.
 */
private val TipografiaKotril = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 1.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp
    )
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se desactiva el color dinamico: la identidad visual de
    // Kotril debe verse igual en todos los dispositivos.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquema = if (darkTheme) EsquemaOscuro else EsquemaClaro

    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = esquema,
        typography = TipografiaKotril,
        content = content
    )
}
