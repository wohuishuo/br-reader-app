package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.ui.component.BookCard
import com.bookrealm.reader.ui.component.LoadingBox
import com.bookrealm.reader.ui.component.StateBox
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.SearchField
import com.bookrealm.reader.ui.testing.TestTags

@Composable
fun StoreScreen(
    books: UiState<List<BookItemDto>>,
    query: String,
    onSearch: (String) -> Unit,
    onOpen: (Long) -> Unit,
) {
    var input by remember(query) { mutableStateOf(query) }
    Column(
        Modifier.fillMaxSize().padding(BrDimens.PagePadding),
        verticalArrangement = Arrangement.spacedBy(BrDimens.GapLg),
    ) {
        SearchField(
            value = input,
            onValueChange = { input = it },
            onSearch = { onSearch(input) },
            placeholder = "搜索书名,例如 西游",
            modifier = Modifier.testTag(TestTags.StoreSearch),
        )
        when (books) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> StateBox("书城加载失败", books.message, action = "重试", onAction = { onSearch(input) })
            is UiState.Success -> {
                if (books.data.isEmpty()) {
                    StateBox("没有找到书", "换一个关键词再试一次。", action = "清空搜索", onAction = { input = ""; onSearch("") })
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(BrDimens.GapMd)) {
                        items(books.data, key = { it.id }) { book ->
                            BookCard(book = book, onClick = { onOpen(book.id) })
                        }
                    }
                }
            }
        }
    }
}
