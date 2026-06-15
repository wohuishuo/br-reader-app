package com.bookrealm.reader.ui.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun BrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    preset: String = "purple",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> darkSchemeFor(preset)
        else -> lightSchemeFor(preset)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

private fun lightSchemeFor(preset: String) = when (preset) {
    "teal" -> lightColorScheme(primary = BrColors.BrandTeal, secondary = BrColors.BrandPurple, tertiary = BrColors.BrandYellow)
    "blue" -> lightColorScheme(primary = Color(0xFF3C7DFF), secondary = BrColors.BrandTeal, tertiary = BrColors.BrandYellow)
    "amber" -> lightColorScheme(primary = Color(0xFFB26A00), secondary = BrColors.BrandPurple, tertiary = BrColors.BrandTeal)
    else -> BrLightColorScheme
}

private fun darkSchemeFor(preset: String) = when (preset) {
    "teal" -> darkColorScheme(primary = Color(0xFF6FE7CB), secondary = Color(0xFF9B94FF), tertiary = BrColors.BrandYellow)
    "blue" -> darkColorScheme(primary = Color(0xFF9AB8FF), secondary = BrColors.BrandTeal, tertiary = BrColors.BrandYellow)
    "amber" -> darkColorScheme(primary = Color(0xFFFFC66D), secondary = Color(0xFF9B94FF), tertiary = BrColors.BrandTeal)
    else -> BrDarkColorScheme
}
