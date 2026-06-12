package com.bookrealm.reader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 书域品牌色(与平台书一致)
val BrandPurple = Color(0xFF6C63FF)
val BrandTeal = Color(0xFF48CFAD)

private val LightColors = lightColorScheme(
    primary = BrandPurple,
    secondary = BrandTeal,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9B94FF),
    secondary = BrandTeal,
)

@Composable
fun ReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
