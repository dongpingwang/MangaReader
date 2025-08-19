package com.wolf2.reader.ui.common.chapter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.book.Chapter

@Composable
fun ChapterList(chapters: List<Chapter>, onPageChange: (Int) -> Unit) {
    if (chapters.size <= 1) {
        EmptyHint(hint = R.string.empty_charpter_hint)
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(chapters) {
            ChapterTitle(chapterName = it.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onPageChange(it.pageIndexRange.first)
                    })
        }
    }
}