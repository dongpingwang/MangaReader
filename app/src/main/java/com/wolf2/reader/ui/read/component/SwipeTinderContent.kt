package com.wolf2.reader.ui.read.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.spartapps.swipeablecards.state.rememberSwipeableCardsState
import com.spartapps.swipeablecards.ui.SwipeableCardDirection
import com.spartapps.swipeablecards.ui.SwipeableCardsProperties
import com.spartapps.swipeablecards.ui.lazy.LazySwipeableCards
import com.spartapps.swipeablecards.ui.lazy.items
import com.wolf2.reader.config.toContentScale
import com.wolf2.reader.config.toFilterQuality
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import com.wolf2.reader.util.LoadResult

@Composable
fun SwipeTinderContent(
    vm: ReadViewModel,
    uiState: ReadUiState,
    onImageClick: () -> Unit = {}
) {
    val book = (uiState.bookResult as LoadResult.Success<Book>).data
    val background = if (uiState.darkMode) Color.Black else Color.Transparent
    val imageScale = uiState.imageScale.toContentScale()
    val imageQuality = uiState.imageQuality.toFilterQuality()

    val state = rememberSwipeableCardsState(
        initialCardIndex = uiState.readRecord.curPage,
        itemCount = { book.pageContents.size }
    )

    LaunchedEffect(state) {
        snapshotFlow { state.currentCardIndex }.collect {
            vm.updateReadRecord(it)
        }
    }

    LazySwipeableCards<PageContent>(
        properties = SwipeableCardsProperties(padding = 0.dp, stackedCardsOffset = 0.dp),
        state = state,
        onSwipe = { content, direction ->
            when (direction) {
                SwipeableCardDirection.Right -> {}
                SwipeableCardDirection.Left -> {}
            }
        }
    ) {
        items(book.pageContents) { content, index, offset ->
            val buffer = vm.getImageBuffer(content)
            Image(
                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer?.size ?: 0).asImageBitmap(),
                contentScale = imageScale,
                filterQuality = imageQuality,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .combinedClickable(onLongClick = {
                    }, onClick = onImageClick)
            )
        }
    }

}