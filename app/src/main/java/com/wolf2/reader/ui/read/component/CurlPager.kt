package com.wolf2.reader.ui.read.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wolf2.reader.mode.entity.book.PageContent
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPageCurlApi::class)
@Composable
fun CurlPager(
    isDarkMode: Boolean,
    curPage: Int,
    pageContents: List<PageContent>,
    onLoadImage: (PageContent) -> Bitmap?,
    onImageClick: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pageState = rememberPageCurlState(initialCurrent = curPage)
    val background = if (isDarkMode) Color.Black else Color.Transparent

    LaunchedEffect(pageState) {
        snapshotFlow { pageState.current }.collect {
            onPageChange(it)
        }
    }
    scope.launch {
        pageState.snapTo(curPage)
    }

    val modifier = Modifier
        .fillMaxSize()
        .background(background)
        .clickable {
            onImageClick()
        }
    // TODO 点击事件与Zoomable冲突
    PageCurl(count = pageContents.size, state = pageState) {
        ReaderPageImage(
            pageContent = pageContents[it],
            onLoadImage = onLoadImage,
            modifier = modifier
        )
    }
}