package com.wolf2.reader.ui.shelf.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.reader.percent
import com.wolf2.reader.ui.shelf.BookShelfUiState
import my.nanihadesuka.compose.LazyVerticalGridScrollbar

@Composable
fun ShelfGridContent(
    uiState: BookShelfUiState,
    bookPagingItems: LazyPagingItems<Book>,
    column: Int,
    onItemClick: (Book) -> Unit = {},
    onItemLongClick: (Book) -> Unit = {},
) {
    val readRecords = uiState.readRecords
    val gridState = rememberLazyGridState()

    LazyVerticalGridScrollbar(state = gridState) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(column),
            state = gridState,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        ) {
            items(
                bookPagingItems.itemCount,
                key = bookPagingItems.itemKey { it.uuid }
            ) { index ->
                bookPagingItems[index]?.let { book ->
                    GridItem(
                        book = book,
                        readRecord = readRecords.firstOrNull { it.bookUuid == book.uuid },
                        onItemClick = onItemClick,
                        onItemLongClick = onItemLongClick
                    )
                }
            }
        }
    }
}

@Composable
private fun GridItem(
    book: Book,
    readRecord: ReadRecord?,
    onItemClick: (Book) -> Unit = {},
    onItemLongClick: (Book) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .combinedClickable(onClick = {
                onItemClick(book)
            }, onLongClick = {
                onItemLongClick(book)
            })
    ) {
        Box {
            AsyncImage(
                model = book.cover.getImageSource(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .padding(end = 4.dp, bottom = 4.dp)
                    .size(24.dp)
                    .background(
                        ProgressIndicatorDefaults.circularColor,
                        shape = CircleShape
                    )
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                val process = readRecord.percent()
                Text(
                    text = process,
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 8.sp
                )
            }
        }

        Text(
            text = book.title,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.StartEllipsis,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }

}