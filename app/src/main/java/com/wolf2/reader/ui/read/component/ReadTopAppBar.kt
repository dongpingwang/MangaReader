package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.wolf2.reader.R
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.read.ReadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoxScope.ReadTopAppBar(readViewModel: ReadViewModel) {
    var showMenuDrop by remember { mutableStateOf(false) }

    TopAppBar(title = {}, navigationIcon = {
        IconButton(onClick = {
            globalViewContext?.navController?.popBackStack()
        }) {
            Icon(imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = null)
        }
    }, actions = {
        IconButton(onClick = {

        }) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null
            )
        }

        IconButton(onClick = { showMenuDrop = true }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert, contentDescription = null
            )
        }
    })

    ReadDropMenu(readViewModel = readViewModel, showMenuDrop = showMenuDrop) {
        showMenuDrop = false
    }
}