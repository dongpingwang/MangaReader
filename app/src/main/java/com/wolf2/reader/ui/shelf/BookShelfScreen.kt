package com.wolf2.reader.ui.shelf

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wolf2.reader.R
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.common.LoadingIndicator
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.theme.Typography
import com.wolf2.reader.ui.util.UiConstants
import com.wolf2.reader.ui.util.getReadProgress
import com.wolf2.reader.ui.util.getReadStatus
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
fun BookShelfScreen() {
    val viewModel: BookShelfViewModel = viewModel(
        factory = BookShelfViewModel.provideFactory(),
        viewModelStoreOwner = globalViewContext?.owner ?: LocalViewModelStoreOwner.current!!
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.loadStatus.isLoading() -> LoadingIndicator()
        uiState.books.isEmpty() -> EmptyHint {
            viewModel.onEvent(
                BookShelfUiEvent.OnNavigationToBrowser
            )
        }

        else -> ShelfContent(uiState)
    }
}

@Composable
private fun ShelfContent(uiState: BookShelfUiState) {
    val books = uiState.books
    val readRecords = uiState.readRecords
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val listState = rememberLazyListState()
        LazyColumnScrollbar(state = listState) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = UiConstants.SCREEN_MARGIN,
                    vertical = UiConstants.VIEW_MARGIN
                )
            ) {
                items(books) { book ->
                    ShelfItem(
                        book = book,
                        readRecord = readRecords.firstOrNull { it.bookUuid == book.uuid })
                }
            }
        }
    }
}

@Composable
private fun ShelfItem(book: Book, readRecord: ReadRecord?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = UiConstants.VIEW_MARGIN)
            .combinedClickable(onClick = {
                globalViewContext?.navController?.navigate("${Routes.READ}/${book.uuid}")
            }, onLongClick = {

            })
    ) {

        AsyncImage(
            model = book.cover.getImageSource(),
            contentDescription = null,
            alignment = Alignment.TopStart,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .width(80.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(UiConstants.CORNER))
        )
        Column(
            modifier = Modifier
                .padding(horizontal = UiConstants.VIEW_MARGIN)
                .height(120.dp)
        ) {
            Text(text = book.title, style = Typography.bodyLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(UiConstants.ALPHA)
                )
                Text(
                    text = book.author,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .alpha(UiConstants.ALPHA)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(UiConstants.ALPHA)
                )
                Text(
                    text = readRecord.getReadProgress(),
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .alpha(UiConstants.ALPHA)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(UiConstants.ALPHA)
                )
                Text(
                    text = readRecord.getReadStatus(),
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .alpha(UiConstants.ALPHA)
                )
            }
        }
    }
}

@Composable
private fun EmptyHint(onNavigationToBrowser: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TextButton(onClick = onNavigationToBrowser) {
            Text(text = stringResource(R.string.btn_browser_book))
        }
    }
}