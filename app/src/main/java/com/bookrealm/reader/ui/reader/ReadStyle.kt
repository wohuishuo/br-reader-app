package com.bookrealm.reader.ui.reader

import androidx.compose.ui.graphics.Color
import com.bookrealm.reader.ui.theme.ReaderTokens

enum class ReaderPalette(
    val label: String,
    val background: Color,
    val foreground: Color,
    val muted: Color,
) {
    Paper("纸页", ReaderTokens.PaperBackground, ReaderTokens.PaperForeground, ReaderTokens.PaperMuted),
    Green("护眼", ReaderTokens.GreenBackground, ReaderTokens.GreenForeground, ReaderTokens.GreenMuted),
    Night("夜间", ReaderTokens.NightBackground, ReaderTokens.NightForeground, ReaderTokens.NightMuted),
}

data class ReadStyle(
    val palette: ReaderPalette = ReaderPalette.Paper,
    val fontScale: Float = 1.0f,
    val lineScale: Float = 1.0f,
)
