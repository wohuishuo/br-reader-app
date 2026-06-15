package com.bookrealm.reader.ui.design

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object BrColors {
    val BrandPurple = Color(0xFF6C63FF)
    val BrandTeal = Color(0xFF48CFAD)
    val BrandYellow = Color(0xFFFED766)

    val ReaderPaper = Color(0xFFFBF8F1)
    val ReaderPaperText = Color(0xFF1F1B16)
    val ReaderPaperMuted = Color(0xFF7A7165)
    val ReaderGreen = Color(0xFFEAF2E6)
    val ReaderGreenText = Color(0xFF1D241A)
    val ReaderGreenMuted = Color(0xFF68735F)
    val ReaderNight = Color(0xFF141414)
    val ReaderNightText = Color(0xFFE7E0D6)
    val ReaderNightMuted = Color(0xFFAAA197)

    val Selection = Color(0x663C8DFF)
    val Highlight = Color(0x33FED766)
    val AiScrim = Color.Black
    val AiSurface = Color(0xFF171717)
    val AiInput = Color(0xFF1D1D1D)
    val AiBorder = Color(0xFF333333)
    val AiTextMuted = Color(0xFFD8D8D8)
    val ActionDock = Color(0xEE2F2F2F)
}

object BrDimens {
    val PagePadding = 16.dp
    val PagePaddingLarge = 22.dp
    val GapXs = 4.dp
    val GapSm = 8.dp
    val GapMd = 12.dp
    val GapLg = 16.dp
    val GapXl = 20.dp
    val IconButton = 48.dp
    val FabSmall = 48.dp
    val Hairline = 1.dp
    val ReaderTopBarHeight = 58.dp
    val ReaderBottomBarHeight = 60.dp
    val BookCoverWidth = 70.dp
    val BookCoverHeight = 96.dp
}

object BrShapes {
    val Xs = RoundedCornerShape(6.dp)
    val Sm = RoundedCornerShape(8.dp)
    val Md = RoundedCornerShape(12.dp)
    val Lg = RoundedCornerShape(18.dp)
    val Xl = RoundedCornerShape(28.dp)
}

object BrMotion {
    val Standard = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val Expressive = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}

val BrLightColorScheme: ColorScheme = lightColorScheme(
    primary = BrColors.BrandPurple,
    secondary = BrColors.BrandTeal,
    tertiary = BrColors.BrandYellow,
)

val BrDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF9B94FF),
    secondary = BrColors.BrandTeal,
    tertiary = BrColors.BrandYellow,
)
