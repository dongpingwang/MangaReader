package com.wolf2.reader.ui.detail.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.detail.DetailUiState
import com.wolf2.reader.util.LoadResult

@Composable
internal fun DetailContent(
    uiState: DetailUiState,
    onTitleChange: (String) -> Unit = {},
    onAuthorChange: (String) -> Unit = {},
    onFavoriteChange: (Boolean) -> Unit = {}
) {
    val book = (uiState.bookResult as LoadResult.Success<Book>).data
    Column {
        BookMetadata(
            book = book,
            favorite = uiState.favorite,
            onTitleChange = onTitleChange,
            onAuthorChange = onAuthorChange,
            onFavoriteChange = onFavoriteChange
        )
        BookSource(book = book, readRecord = uiState.readRecord)
    }
}