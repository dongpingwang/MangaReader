package com.wolf2.reader.ui.common.chapter

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.BookMark
import com.wolf2.reader.mode.entity.book.PageContent

const val columns = 5

@Composable
fun BookMarkGrid(
    bookMarks: List<BookMark>,
    pageContents: List<PageContent>,
    onPageChange: (Int) -> Unit,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onLoadBitmap: (PageContent) -> Bitmap?,
) {
    if (bookMarks.isEmpty()) {
        EmptyHint(hint = R.string.empty_bookmark_hint)
        return
    }
    val map = bookMarks.groupBy { it.chapterName }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        map.keys.forEach { chapterName ->
            item(span = { GridItemSpan(columns) }) {
                ChapterTitle(chapterName = chapterName)
            }
            map[chapterName]?.let { marks ->
                items(marks) { mark ->
                    val pageIndex = mark.pageIndex
                    val pageContent = pageContents[mark.pageIndex]
                    CardImage(
                        pageIndex = pageIndex,
                        pageContent = pageContent,
                        onPageChange = onPageChange,
                        onLoadBuffer = onLoadBuffer,
                        onLoadBitmap = onLoadBitmap,
                    )
                }
            }
        }
    }
}