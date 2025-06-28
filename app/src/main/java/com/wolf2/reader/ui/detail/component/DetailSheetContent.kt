package com.wolf2.reader.ui.detail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R
import me.saket.cascade.CascadeDropdownMenu

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailSheetContent(
    onNavigationToRead: () -> Unit,
    onCopyContent: () -> Unit,
    onDeleteReadRecord: () -> Unit,
    onShareBookFile: () -> Unit,
    onCreateShortcut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        var checked by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.FormatListBulleted, contentDescription = null)
            }

            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.GridView, contentDescription = null)
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1F))

            Box(modifier = Modifier.wrapContentSize()) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(
                            onClick = onNavigationToRead,
                        ) {
                            Text(text = stringResource(R.string.sbl_read))
                        }
                    },
                    trailingButton = {
                        SplitButtonDefaults.TrailingButton(
                            checked = checked,
                            onCheckedChange = { checked = it },
                        ) {
                            val rotation by animateFloatAsState(targetValue = if (checked) 180f else 0f)
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                modifier =
                                    Modifier
                                        .size(SplitButtonDefaults.TrailingIconSize)
                                        .graphicsLayer {
                                            this.rotationZ = rotation
                                        },
                                contentDescription = null
                            )
                        }
                    }
                )

                if (checked) {
                    SblDropMenu(
                        onCopyContent = onCopyContent,
                        onDeleteReadRecord = onDeleteReadRecord,
                        onShareBookFile = onShareBookFile,
                        onCreateShortcut = onCreateShortcut,
                        onDismissRequest = {
                            checked = false
                        })
                }
            }
        }
    }
}

@Composable
private fun SblDropMenu(
    onCopyContent: () -> Unit,
    onDeleteReadRecord: () -> Unit,
    onShareBookFile: () -> Unit,
    onCreateShortcut: () -> Unit,
    onDismissRequest: () -> Unit
) {
    CascadeDropdownMenu(expanded = true, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_content_copy)) },
            onClick = {
                onCopyContent()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_delete_read_record)) },
            onClick = {
                onDeleteReadRecord()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_share)) },
            onClick = {
                onShareBookFile()
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_shortcut)) },
            onClick = {
                onCreateShortcut()
                onDismissRequest()
            }
        )
    }
}