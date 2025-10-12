package com.wolf2.reader.ui.read.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.PageImage
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun TinderPager(
    isDarkMode: Boolean,
    curPage: Int,
    pageContents: List<PageContent>,
    onLoadImage: (PageContent) -> Bitmap,
    onImageClick: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    val state = rememberSwipeableCardsState(
        initialCardIndex = curPage,
        itemCount = { pageContents.size }
    )
    val background = if (isDarkMode) Color.Black else Color.Transparent
    val scope = rememberCoroutineScope()
    scope.launch {
        state.setCurrentIndex(curPage)
    }
    LaunchedEffect(state) {
        snapshotFlow { state.currentCardIndex }.collect {
            onPageChange(it)
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
        items(pageContents) { content, index, offset ->
            PageImage(
                bitmap = onLoadImage(content)
                    .asImageBitmap(),
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .zoomable(zoomState = rememberZoomState(), onTap = {
                        onImageClick()
                    })
            )
        }
    }

}