package com.bookrealm.reader.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.bookrealm.reader.ui.design.BrTheme

// 书域品牌色(与平台书一致)
val BrandPurple = Color(0xFF6C63FF)
val BrandTeal = Color(0xFF48CFAD)

@Composable
fun ReaderTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    preset: String = "purple",
    content: @Composable () -> Unit
) {
    BrTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, preset = preset, content = content)
}
