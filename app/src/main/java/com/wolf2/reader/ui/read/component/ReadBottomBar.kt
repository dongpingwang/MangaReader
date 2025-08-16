package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FirstPage
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.LastPage
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    var sliderPosition by remember { mutableFloatStateOf(curPage.toFloat()) }
    // var steps = (chapterRange.last - chapterRange.first - 1).coerceAtLeast(0)
    val enabled = (chapterRange.last - chapterRange.first) > 0

    BottomAppBar {
        IconButton(onClick = onPrevChapter) {
            Icon(imageVector = Icons.Outlined.FirstPage, contentDescription = null)
        }
        Slider(
            value = sliderPosition,
            valueRange = chapterRange.first.toFloat()..chapterRange.last.toFloat(),
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                onProgressChange(sliderPosition.toInt())
            },
            interactionSource = interactionSource,
            modifier = Modifier.weight(1F),
            thumb = {
                Label(
                    label = {
                        PlainTooltip(
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp, 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sliderPosition.toInt().toString())
                            }
                        }
                    },
                    interactionSource = interactionSource,
                ) {
                    SliderDefaults.Thumb(interactionSource = interactionSource)
                }
            },
            enabled = enabled
        )
        IconButton(onClick = onNextChapter) {
            Icon(imageVector = Icons.Outlined.LastPage, contentDescription = null)
        }

        IconButton(onClick = onShowChapter) {
            Icon(imageVector = Icons.Outlined.FormatListBulleted, contentDescription = null)
        }

        IconButton(onClick = onMoreActionClick) {
            Icon(imageVector = Icons.Outlined.MoreVert, null)
        }
    }
}