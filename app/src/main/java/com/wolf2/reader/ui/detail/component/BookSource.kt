package com.wolf2.reader.ui.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.util.storagePath
import me.saket.bytesize.binaryBytes
import kotlin.math.roundToInt

@Composable
internal fun BookSource(book: Book, readRecord: ReadRecord?) {
    var processF = 0F
    var process = "0%"
    if (readRecord != null) {
        processF = readRecord.curPage.toFloat().div(readRecord.pageCount)
        process = processF.times(100).roundToInt().coerceIn(0, 100).toString() + "%"
    }

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(8.dp)
    ) {
        val path = book.uri.storagePath()?.removePrefix("/storage/emulated/0/") ?: ""

        Row {
            Text(
                text = stringResource(R.string.source),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                path,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val size = DocumentFile.fromSingleUri(LocalContext.current, book.uri)
            ?.length()?.binaryBytes.toString()
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Text(text = stringResource(R.string.size), style = MaterialTheme.typography.bodyMedium)
            Text(
                size,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = stringResource(R.string.page_count),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.read_progress_chapter_size,
                    book.extraInfo.pageCount,
                    book.extraInfo.chapterCount
                ),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.read_progress),
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = { processF },
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 8.dp),
            )
            Text(process, style = MaterialTheme.typography.bodyMedium)
        }
    }
}