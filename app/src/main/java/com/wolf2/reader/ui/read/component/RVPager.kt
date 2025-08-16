package com.wolf2.reader.ui.read.component

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
import com.wolf2.reader.mode.entity.book.PageContent
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun RVPager(
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
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect {
            onPageChange(it)
        }
    }
    LazyColumn(state = listState) {
        items(pageContents) {
            PageImage(
                bitmap = onLoadImage(it).asImageBitmap(),
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState = rememberZoomState(), onTap = { onImageClick() })
            )
        }
    }

}

