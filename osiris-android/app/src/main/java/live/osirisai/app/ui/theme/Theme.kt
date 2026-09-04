package live.osirisai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val OsirisBlack = Color(0xFF0A0A0A)
val OsirisCharcoal = Color(0xFF121212)
val OsirisPanel = Color(0xE6121212)
val OsirisGold = Color(0xFFE8B84A)
val OsirisGoldDim = Color(0xFFB89030)
val OsirisCyan = Color(0xFF4ECBDE)
val OsirisText = Color(0xFFE8E6E1)
val OsirisMuted = Color(0xFF8A8780)
val OsirisDanger = Color(0xFFE85A4A)
val OsirisHairline = Color(0x33E8E6E1)

private val OsirisColorScheme = darkColorScheme(
    primary = OsirisGold,
    onPrimary = OsirisBlack,
    secondary = OsirisCyan,
    onSecondary = OsirisBlack,
    background = OsirisBlack,
    onBackground = OsirisText,
    surface = OsirisCharcoal,
    onSurface = OsirisText,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = OsirisMuted,
    error = OsirisDanger,
    onError = OsirisText,
    outline = OsirisHairline,
)

private val OsirisTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 40.sp,
        letterSpacing = 4.sp,
        color = OsirisText,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        letterSpacing = 1.sp,
        color = OsirisText,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp,
        color = OsirisText,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = OsirisText,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = OsirisMuted,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        color = OsirisGold,
    ),
)

@Composable
fun OsirisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OsirisColorScheme,
        typography = OsirisTypography,
        content = content,
    )
}
