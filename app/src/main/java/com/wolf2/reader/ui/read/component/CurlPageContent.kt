package com.wolf2.reader.ui.read.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.wolf2.reader.config.toContentScale
import com.wolf2.reader.config.toFilterQuality
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import com.wolf2.reader.util.LoadResult
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPageCurlApi::class)
@Composable
fun CurlPageContent(
    viewModel: ReadViewModel,
    uiState: ReadUiState,
    onImageClick: () -> Unit = {}
) {
    val book = (uiState.bookResult as LoadResult.Success<Book>).data
    val scope = rememberCoroutineScope()
    val pageState = rememberPageCurlState(initialCurrent = uiState.curPage)
    val background = if (uiState.darkMode) Color.Black else Color.Transparent
    val imageScale = uiState.imageScale.toContentScale()
    val imageQuality = uiState.imageQuality.toFilterQuality()

    LaunchedEffect(pageState) {
        snapshotFlow { pageState.current }.collect {
            viewModel.updateReadRecord(it)
        }
    }
    LaunchedEffect(uiState.curPage, pageState.max) {
        snapshotFlow { pageState.max }.collect {
            if (it > 0) {
                scope.launch { pageState.snapTo(uiState.curPage) }
            }
        }
        snapshotFlow { uiState.curPage }.collect {
            if (pageState.max > 0) {
                scope.launch { pageState.snapTo(it) }
            }
        }
    }

    PageCurl(count = book.pageContents.size, state = pageState) {
        val content = book.pageContents[it]
        val buffer = viewModel.getImageBuffer(content)
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