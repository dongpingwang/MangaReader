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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.PageContent

@Composable
fun PageGrid(
    chapters: List<Chapter>,
    pageContents: List<PageContent>,
    onPageChange: (Int) -> Unit,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onLoadBitmap: (PageContent) -> Bitmap?,
) {
    if (chapters.size <= 1) {
        EmptyHint(hint = R.string.empty_charpter_hint)
        return
    }
    val pageSize = 2
    val paging = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = pageSize,
            prefetchDistance = pageSize,
        ),
        pagingSourceFactory = { ChapterPagingSource(chapters.toPages().also {}) }
    )
    val pagingItems = paging.flow.collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(pagingItems.itemCount, span = { i ->
            val page = pagingItems[i]
            if (page?.title != null) {
                GridItemSpan(columns)
            } else {
                GridItemSpan(1)
            }
        }) { i ->
            val page = pagingItems[i] ?: return@items
            if (page.title != null) {
                ChapterTitle(chapterName = page.title)
            } else {
                val pageContent = pageContents[i]
                CardImage(
                    pageIndex = i,
                    pageContent = pageContent,
                    onPageChange = onPageChange,
                    onLoadBuffer = onLoadBuffer,
                    onLoadBitmap = onLoadBitmap,
                )
            }
        }
    }
}
