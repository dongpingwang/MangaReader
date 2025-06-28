package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadTopAppBar(
    onBackHandle: () -> Unit,
    onShowDrop: () -> Unit
) {

    TopAppBar(title = {}, navigationIcon = {
        IconButton(onClick = onBackHandle) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
        }
    }, actions = {
        IconButton(onClick = {

        }) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null
            )
        }

        IconButton(onClick = onShowDrop) {
            Icon(
                imageVector = Icons.Outlined.MoreVert, contentDescription = null
            )
        }
    })
}