package com.wolf2.reader.ui.common.chapter

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.index
import com.wolf2.reader.util.length

// todo 加载图片可能出现OOM
@Composable
fun PageGrid(
    chapters: List<Chapter>,
    pageContents: List<PageContent>,
    onPageChange: (Int) -> Unit,
    onLoadImage: (PageContent) -> Bitmap,
) {
    if (chapters.size <= 1) {
        EmptyHint(hint = R.string.empty_charpter_hint)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        chapters.fastForEach { chapter ->
            item(span = { GridItemSpan(columns) }) {
                ChapterTitle(chapterName = chapter.title)
            }
            items(count = chapter.pageIndexRange.length()) { i ->
                val pageIndex = chapter.pageIndexRange.index(i)
                val pageContent = pageContents[pageIndex]
                CardImage(
                    pageIndex = pageIndex,
                    pageContent = pageContent,
                    onPageChange = onPageChange,
                    onLoadImage = onLoadImage
                )
            }
        }
    }
}
