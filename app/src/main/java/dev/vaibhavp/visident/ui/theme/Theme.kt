package dev.vaibhavp.visident.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VisidentLightTeal,
    secondary = VisidentLightGreen,
    tertiary = VisidentLightBlue,
    // Consider defining onPrimary, onSecondary, onTertiary, etc.
    // if default contrasts are not sufficient (e.g., using Black or White from Color.kt)
    // onPrimary = Black, 
    // onSecondary = Black,
    // onTertiary = Black,
    // background = Color(0xFF121212), // Example dark background
    // surface = Color(0xFF1E1E1E),   // Example dark surface
    // onBackground = White,
    // onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = VisidentTeal,
    secondary = VisidentGreen,
    tertiary = VisidentBlue

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White, // Ensure good contrast with VisidentTeal
    onSecondary = Color.White, // Ensure good contrast with VisidentGreen
    onTertiary = Color.White, // Ensure good contrast with VisidentBlue
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun VisidentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}