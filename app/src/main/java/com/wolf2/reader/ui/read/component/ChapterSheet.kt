package com.wolf2.reader.ui.read.component

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wolf2.reader.mode.entity.BookMark
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.chapter.HeadButtons
import com.wolf2.reader.ui.common.chapter.SheetContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSheet(
    book: Book,
    bookMarks: List<BookMark>,
    onDismissRequest: () -> Unit,
    onPageChange: (Int) -> Unit,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onLoadBitmap: (PageContent) -> Bitmap?,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        HeadButtons(
            selectedIndex = selectedIndex,
            onSelectChange = { selectedIndex = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SheetContent(
            selectedIndex = selectedIndex,
            chapters = book.chapters,
            pageContents = book.pageContents,
            bookMarks = bookMarks,
            onSelectChange = { selectedIndex = it },
            onPageChange = onPageChange,
            onLoadBuffer = onLoadBuffer,
            onLoadBitmap = onLoadBitmap,
        )
    }
}








