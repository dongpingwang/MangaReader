package com.wolf2.reader.ui.home.component


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R

@Composable
fun HomeFab(
    onNavigationToRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onNavigationToRead,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(imageVector = Icons.Outlined.LocalLibrary, contentDescription = null)
            Text(
                text = stringResource(R.string.fab_continue_read),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

}