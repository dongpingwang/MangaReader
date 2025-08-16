package com.wolf2.reader.ui.shelf.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.reader.percent
import com.wolf2.reader.ui.shelf.BookShelfUiState
import com.wolf2.reader.ui.theme.Typography
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
fun ShelfListContent(
    uiState: BookShelfUiState,
    bookPagingItems: LazyPagingItems<Book>,
    onItemClick: (Book) -> Unit = {},
    onItemLongClick: (Book) -> Unit = {}
) {
    val readRecords = uiState.readRecords
    val listState = rememberLazyListState()
    LazyColumnScrollbar(state = listState) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
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
                    ListItem(
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
private fun ListItem(
    book: Book,
    readRecord: ReadRecord?,
    onItemClick: (Book) -> Unit = {},
    onItemLongClick: (Book) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(onClick = {
                onItemClick(book)
            }, onLongClick = {
                onItemLongClick(book)
            })
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(120.dp)
        ) {
            AsyncImage(
                model = book.cover.getImageSource(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .size(24.dp)
                    .background(ProgressIndicatorDefaults.circularColor, shape = CircleShape)
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
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(120.dp)
        ) {
            Text(text = book.title)
            Text(
                text = book.author,
                style = Typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
