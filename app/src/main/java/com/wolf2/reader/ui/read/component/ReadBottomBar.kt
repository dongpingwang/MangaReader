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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ReadBottomBar(
    curPage: Int,
    chapterRange: IntRange,
    onPrevChapter: () -> Unit,
    onProgressChange: (Int) -> Unit,
    onNextChapter: () -> Unit,
    onShowChapter: () -> Unit,
    onMoreActionClick: () -> Unit = {}
) {
    var sliderPosition by remember { mutableFloatStateOf(curPage.toFloat()) }
//    var steps = (chapterRange.last - chapterRange.first - 1).coerceAtLeast(0)
//    if (steps >= 30) {
//        steps = 0
//    }

    BottomAppBar {
        IconButton(onClick = onPrevChapter) {
            Icon(imageVector = Icons.Outlined.SkipPrevious, contentDescription = null)
        }
        Slider(
            value = sliderPosition,
            valueRange = chapterRange.first.toFloat()..chapterRange.last.toFloat(),
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                onProgressChange(sliderPosition.toInt())
            },
            modifier = Modifier.weight(1F)
        )
        IconButton(onClick = onNextChapter) {
            Icon(imageVector = Icons.Outlined.SkipNext, contentDescription = null)
        }

        IconButton(onClick = onShowChapter) {
            Icon(imageVector = Icons.Outlined.FormatListBulleted, contentDescription = null)
        }

        IconButton(onClick = onMoreActionClick) {
            Icon(imageVector = Icons.Outlined.MoreVert, null)
        }
    }
}