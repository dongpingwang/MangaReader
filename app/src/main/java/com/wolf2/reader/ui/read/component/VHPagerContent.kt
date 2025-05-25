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
import com.wolf2.reader.convert.toImageBuffer
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import timber.log.Timber

@Composable
internal fun VHPagerContent(
    readViewModel: ReadViewModel,
    isVerticalPager: Boolean,
    readUiState: ReadUiState,
    onImageClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = readUiState.curPage) { readUiState.book.pageCount() }
    val background = if (readUiState.darkMode) Color.Black else Color.Transparent
    val imageScale = readUiState.imageScale.toContentScale()
    val imageQuality = readUiState.imageQuality.toFilterQuality()
    LaunchedEffect(readUiState) {
        snapshotFlow { readUiState.curPage }.collect {
            pagerState.scrollToPage(it)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            readViewModel.updateReadRecord(it)
        }
    }
    if (isVerticalPager) {
        VerticalPager(pagerState) {
            val content = readUiState.book.pageContents[it]
            AsyncImage(
                model = content.toImageBuffer(),
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
            val content = readUiState.book.pageContents[it]
            AsyncImage(
                model = content.toImageBuffer(),
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