package com.wolf2.reader.ui.read.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wolf2.reader.mode.entity.book.PageContent
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun VHPager(
    isDarkMode: Boolean,
    isVerticalPager: Boolean,
    curPage: Int,
    pageContents: List<PageContent>,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onLoadBitmap: (PageContent) -> Bitmap?,
    onImageClick: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = curPage) { pageContents.size }
    val background = if (isDarkMode) Color.Black else Color.Transparent
    val scope = rememberCoroutineScope()
    scope.launch {
        pagerState.scrollToPage(curPage)
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            onPageChange(it)
        }
    }

    val modifier = Modifier
        .fillMaxSize()
        .background(background)
        .zoomable(zoomState = rememberZoomState(), onTap = {
            onImageClick()
        })


    if (isVerticalPager) {
        VerticalPager(pagerState) {
            ReaderPageAsyncImage(
                pageContent = pageContents[it],
                onLoadBuffer = onLoadBuffer,
                onLoadBitmap = onLoadBitmap,
                modifier = modifier
            )
        }
    } else {
        HorizontalPager(pagerState) {
            ReaderPageAsyncImage(
                pageContent = pageContents[it],
                onLoadBuffer = onLoadBuffer,
                onLoadBitmap = onLoadBitmap,
                modifier = modifier
            )
        }
    }
}