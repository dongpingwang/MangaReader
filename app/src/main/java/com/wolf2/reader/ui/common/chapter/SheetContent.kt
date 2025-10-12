package com.wolf2.reader.ui.common.chapter

import android.graphics.Bitmap
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import com.wolf2.reader.mode.entity.BookMark
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.PageContent
import kotlinx.coroutines.launch

@Composable
fun SheetContent(
    selectedIndex: Int,
    chapters: List<Chapter>,
    pageContents: List<PageContent>,
    bookMarks: List<BookMark>,
    onSelectChange: (Int) -> Unit,
    onPageChange: (Int) -> Unit,
    onLoadBuffer: (PageContent) -> ByteArray?,
) {
    val pagerState = rememberPagerState(initialPage = selectedIndex) { 2 }
    val scope = rememberCoroutineScope()
    scope.launch { pagerState.scrollToPage(selectedIndex) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            onSelectChange(it)
        }
    }
    HorizontalPager(state = pagerState) {
        when (it) {
            0 -> ChapterList(
                chapters = chapters,
                onPageChange = onPageChange
            )

//            1 -> PageGrid(
//                chapters = chapters,
//                pageContents = pageContents,
//                onPageChange = onPageChange,
//                onLoadBuffer = onLoadBuffer
//            )

            1 -> BookMarkGrid(
                bookMarks = bookMarks,
                pageContents = pageContents,
                onPageChange = onPageChange,
                onLoadBuffer = onLoadBuffer
            )
        }
    }
}