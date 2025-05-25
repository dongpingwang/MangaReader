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
import com.wolf2.reader.convert.toImageBuffer
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPageCurlApi::class)
@Composable
fun CurlPageContent(
    readViewModel: ReadViewModel,
    readUiState: ReadUiState,
    onImageClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val pageState = rememberPageCurlState(initialCurrent = readUiState.curPage)
    val background = if (readUiState.darkMode) Color.Black else Color.Transparent
    val imageScale = readUiState.imageScale.toContentScale()
    val imageQuality = readUiState.imageQuality.toFilterQuality()

    LaunchedEffect(pageState) {
        snapshotFlow { pageState.current }.collect {
            readViewModel.updateReadRecord(it)
        }
    }
    LaunchedEffect(readUiState.curPage, pageState.max) {
        snapshotFlow { pageState.max }.collect {
            if (it > 0) {
                scope.launch { pageState.snapTo(readUiState.curPage) }
            }
        }
        snapshotFlow { readUiState.curPage }.collect {
            if (pageState.max > 0) {
                scope.launch { pageState.snapTo(it) }
            }
        }
    }

    PageCurl(count = readUiState.book.pageCount(), state = pageState) {
        val content = readUiState.book.pageContents[it]
        val buffer = content.toImageBuffer()
        Image(
            bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer?.size ?: 0).asImageBitmap(),
            contentScale = imageScale,
            filterQuality = imageQuality,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .combinedClickable(onLongClick = {
                    //showBottomSheet = true
                }, onClick = onImageClick)
        )
    }
}