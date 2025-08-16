package com.wolf2.reader.ui.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.ViewComfy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R
import me.saket.cascade.CascadeDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun HomeDropMenu(
    onNavigationToBrowser: () -> Unit = {},
    onNavigationToWifiTransfer: () -> Unit = {},
    onShowLayoutDialog: () -> Unit = {},
    onNavigationToSettings: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    CascadeDropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(
            x = LocalWindowInfo.current.containerSize.width.dp,
            y = (-LocalWindowInfo.current.containerSize.height).dp
        )
    )
    {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_pick_file)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CreateNewFolder,
                    contentDescription = null
                )
            },
            onClick = {
                onNavigationToBrowser()
                onDismissRequest()
            })
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_wifi_transfer)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.SyncAlt,
                    contentDescription = null
                )
            },
            onClick = {
                onNavigationToWifiTransfer()
                onDismissRequest()
            })
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_layout_edit)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ViewComfy,
                    contentDescription = null
                )
            },
            onClick = {
                onShowLayoutDialog()
                onDismissRequest()
            })

        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_settings)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            },
            onClick = {
                onNavigationToSettings()
                onDismissRequest()
            })

    }
}