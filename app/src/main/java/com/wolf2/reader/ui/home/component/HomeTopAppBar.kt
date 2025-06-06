package com.wolf2.reader.ui.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopAppBar(
    curPageIndex: Int,
    onNavigationToSearch: () -> Unit = {},
    onShowSortDialog: () -> Unit = {},
    onShowMenuDialog: () -> Unit = {}
) {

    TopAppBar(title = {
        Text(text = stringResource(if (curPageIndex == 0) R.string.navi_book_shelf else R.string.navi_history))
    }, actions = {
        IconButton(onClick = onNavigationToSearch) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        }
        IconButton(onClick = onShowSortDialog) {
            Icon(
                imageVector = Icons.Outlined.Sort,
                contentDescription = null
            )
        }
        IconButton(onClick = onShowMenuDialog) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null
            )
        }
    })
}