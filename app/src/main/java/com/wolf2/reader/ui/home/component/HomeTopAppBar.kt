package com.wolf2.reader.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
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
fun HomeTopAppBar(
    curPageIndex: Int,
    onNavigationToSearch: () -> Unit,
    onShowFilterDialog: () -> Unit,
    onShowSortDialog: () -> Unit,
    onShowMenuDialog: () -> Unit
) {

    TopAppBar(title = {
        Text(text = stringResource(if (curPageIndex == 0) R.string.navi_book_shelf else R.string.navi_history))
    }, actions = {
        AnimatedVisibility(curPageIndex == 0) {
            Row {
                IconButton(onClick = onNavigationToSearch) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                }
                IconButton(onClick = onShowFilterDialog) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = null
                    )
                }
                IconButton(onClick = onShowSortDialog) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null
                    )
                }
            }
        }
        IconButton(onClick = onShowMenuDialog) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null
            )
        }
    })
}