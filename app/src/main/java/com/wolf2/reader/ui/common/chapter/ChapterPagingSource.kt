package com.wolf2.reader.ui.common.chapter

import androidx.compose.ui.util.fastForEach
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.wolf2.reader.mode.entity.book.Chapter

data class ChapterPage(val title: String? = null, val pageIndex: Int = -1)

fun List<Chapter>.toPages(): List<ChapterPage> {
    val result = mutableListOf<ChapterPage>()
    this.fastForEach {
        result.add(ChapterPage(title = it.title))
        for (i in it.pageIndexRange) {
            result.add(ChapterPage(pageIndex = i))
        }
    }
    return result
}

class ChapterPagingSource(
    private val pages: List<ChapterPage>
) : PagingSource<Int, ChapterPage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChapterPage> {
        return try {
            val pageNumber = params.key ?: 1
            val prevKey = if (pageNumber > 1) pageNumber - 1 else null
            val nextKey =
                if (pages.isNotEmpty() && pages.size >= params.loadSize) pageNumber + 1 else null
            LoadResult.Page(
                data = pages,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ChapterPage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}