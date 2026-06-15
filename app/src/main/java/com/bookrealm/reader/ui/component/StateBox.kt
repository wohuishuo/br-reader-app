package com.bookrealm.reader.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.bookrealm.reader.ui.design.BrButton
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.BrShapes

@Composable
fun LoadingBox(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun StateBox(
    title: String,
    body: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(BrDimens.PagePaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm),
        ) {
            Surface(
                Modifier.size(BrDimens.IconButton),
                shape = BrShapes.Xl,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(BrDimens.GapXs))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null && onAction != null) {
                BrButton(text = action, onClick = onAction)
            }
        }
    }
}
