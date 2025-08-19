package com.wolf2.reader.ui.common.chapter

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ChapterTitle(chapterName: String, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = {
            Text(
                text = chapterName,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ListItemDefaults.colors().copy(containerColor = Color.Transparent),
        modifier = modifier
    )
}