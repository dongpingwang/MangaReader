package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.wolf2.reader.mode.entity.book.PageContent
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun VHPager(
    isVerticalPager: Boolean,
    curPage: Int,
    pageContents: List<PageContent>,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onImageClick: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = curPage) { pageContents.size }
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
        .zoomable(zoomState = rememberZoomState(), onTap = {
            onImageClick()
        })

    if (isVerticalPager) {
        VerticalPager(pagerState) {
            PageAsyncImage(model = onLoadBuffer(pageContents[it]), modifier = modifier)
        }
    } else {
        HorizontalPager(pagerState) {
            PageAsyncImage(model = onLoadBuffer(pageContents[it]), modifier = modifier)
        }
    }
}