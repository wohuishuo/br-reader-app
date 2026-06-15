package com.bookrealm.reader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.ui.theme.ReaderTokens
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.BrShapes

@Composable
fun BookCover(
    title: String,
    modifier: Modifier = Modifier.size(width = BrDimens.BookCoverWidth, height = BrDimens.BookCoverHeight),
    compact: Boolean = false,
) {
    val text = title.take(2).ifBlank { "书" }
    Box(
        modifier.clip(BrShapes.Sm).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
