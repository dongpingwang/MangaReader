package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R
import com.wolf2.reader.ui.read.ReadViewModel

@Composable
internal fun BoxScope.ReadDropMenu(
    readViewModel: ReadViewModel,
    showMenuDrop: Boolean = false,
    onDismissRequest: () -> Unit = {}
) {
    if (showMenuDrop) {
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = onDismissRequest,
                shape = ShapeDefaults.Medium
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_show_big_image)) },
                    onClick = {
                        onDismissRequest()
                    })
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_download_image)) },
                    onClick = {
                        onDismissRequest()
                        readViewModel.cacheImage()
                    })
            }
        }
    }
}