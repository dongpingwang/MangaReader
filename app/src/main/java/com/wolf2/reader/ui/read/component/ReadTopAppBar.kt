package com.wolf2.reader.ui.read.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadTopAppBar(
    onBackHandle: () -> Unit,
) {

    TopAppBar(title = {}, navigationIcon = {
        IconButton(onClick = onBackHandle) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
        }
    }, actions = {})
}