package com.wolf2.reader.ui.read.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import com.wolf2.reader.util.LoadResult

@Composable
fun ReadBottomBar(
    readViewModel: ReadViewModel,
    readUiState: ReadUiState,
    onMoreActionClick: () -> Unit = {}
) {
    val book = (readUiState.bookResult as LoadResult.Success<Book>).data

    val _curPage = (readViewModel.getCurPage() + 1).toFloat()
    var sliderPosition by remember { mutableFloatStateOf(_curPage) }
    LaunchedEffect(readUiState) {
        snapshotFlow { readUiState.curPage }.collect {
            sliderPosition = (it + 1).toFloat()
        }
    }

    BottomAppBar {
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Outlined.SkipPrevious, contentDescription = null)
        }
        Slider(
            value = sliderPosition,
            valueRange = 1F..book.pageContents.size.toFloat(),
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                readViewModel.updateReadRecord(sliderPosition.toInt(), true)
            },
            modifier = Modifier.weight(1F)
        )
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Outlined.SkipNext, contentDescription = null)
        }

        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Outlined.FormatListBulleted, contentDescription = null)
        }

        IconButton(onClick = onMoreActionClick) {
            Icon(imageVector = Icons.Outlined.MoreVert, null)
        }
    }
}