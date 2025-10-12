package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.PageImage
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun RVPager(
    isDarkMode: Boolean,
    curPage: Int,
    pageContents: List<PageContent>,
    onLoadImage: (PageContent) -> Bitmap,
    onImageClick: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    scope.launch {
        listState.scrollToItem(curPage)
    }
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val contentScale = if (isPortrait) ContentScale.FillWidth else ContentScale.FillHeight
    val background = if (isDarkMode) Color.Black else Color.Transparent

    val modifier = Modifier
        .fillMaxSize()
        .background(background)
        .zoomable(zoomState = rememberZoomState(), onTap = { onImageClick() })

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect {
            onPageChange(it)
        }
    }
    LazyColumn(state = listState) {
        items(pageContents) {
            PageImage(
                bitmap = onLoadImage(it).asImageBitmap(),
                contentScale = contentScale,
                modifier = modifier
            )
        }
    }
}


