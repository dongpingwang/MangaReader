package com.wolf2.reader.ui.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wolf2.reader.ui.history.HistoryScreen
import com.wolf2.reader.ui.shelf.BookShelfScreen

@Composable
internal fun ColumnScope.HomeContent(curTab: Int) {
    Box(modifier = Modifier.weight(1F)) {
        when (curTab) {
            0 -> BookShelfScreen()
            1 -> HistoryScreen()
        }
    }
}