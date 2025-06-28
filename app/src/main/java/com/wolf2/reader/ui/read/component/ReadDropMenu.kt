package com.wolf2.reader.ui.read.component

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R
import com.wolf2.reader.ui.read.ReadViewModel
import me.saket.cascade.CascadeDropdownMenu

@Composable
internal fun ReadDropMenu(
    readViewModel: ReadViewModel,
    onDismissRequest: () -> Unit = {}
) {
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