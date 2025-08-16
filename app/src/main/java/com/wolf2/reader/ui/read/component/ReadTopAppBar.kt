package com.wolf2.reader.ui.read.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReadTopAppBar(
    chapterTitle: String,
    progressDesc: String,
    onBackHandle: () -> Unit,
) {
    TopAppBar(title = {
        Text(chapterTitle, style = MaterialTheme.typography.bodyMedium)
    },
        subtitle = {
            Text(progressDesc, style = MaterialTheme.typography.bodySmall)
        },
        navigationIcon = {
            IconButton(onClick = onBackHandle) {
                Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
            }
        })
}