package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R
import com.wolf2.reader.ui.read.ReadViewModel
import me.saket.cascade.CascadeDropdownMenu

@Composable
internal fun BoxScope.ReadDropMenu(
    readViewModel: ReadViewModel,
    showMenuDrop: Boolean = false,
    onDismissRequest: () -> Unit = {}
) {
    if (showMenuDrop) {
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            CascadeDropdownMenu(
                expanded = true,
                onDismissRequest = onDismissRequest,
                shape = ShapeDefaults.Medium
            ) {
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