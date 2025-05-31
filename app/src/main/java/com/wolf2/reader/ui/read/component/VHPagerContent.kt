package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.wolf2.reader.config.toContentScale
import com.wolf2.reader.config.toFilterQuality
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import com.wolf2.reader.util.LoadResult

@Composable
internal fun VHPagerContent(
    videModel: ReadViewModel,
    isVerticalPager: Boolean,
    uiState: ReadUiState,
    onImageClick: () -> Unit = {}
) {
    val book = (uiState.bookResult as LoadResult.Success<Book>).data
    val pagerState =
        rememberPagerState(initialPage = uiState.readRecord.curPage) { book.pageContents.size }
    val background = if (uiState.darkMode) Color.Black else Color.Transparent
    val imageScale = uiState.imageScale.toContentScale()
    val imageQuality = uiState.imageQuality.toFilterQuality()
    LaunchedEffect(uiState) {
        snapshotFlow { uiState.curPage }.collect {
            pagerState.scrollToPage(it)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            videModel.updateReadRecord(it)
        }
    }
    if (isVerticalPager) {
        VerticalPager(pagerState) {
            val content = book.pageContents[it]
            AsyncImage(
                model = videModel.getImageBuffer(content),
                contentScale = imageScale,
                filterQuality = imageQuality,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .clickable {
                        onImageClick()
                    }
            )
        }
    } else {
        HorizontalPager(pagerState) {
            val content = book.pageContents[it]
            AsyncImage(
                model = videModel.getImageBuffer(content),
                contentScale = imageScale,
                filterQuality = imageQuality,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .clickable {
                        onImageClick()
                    }
            )
        }
    }
}