package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoreActionSheet(
    marked: Boolean,
    pageSwitchEffect: Int,
    onDismissRequest: () -> Unit = {},
    onSavePicture: () -> Unit = {},
    onBookMarkToggle: () -> Unit = {},
    onJumpPage: () -> Unit = {},
    onPageSwitchEffectChange: (Int) -> Unit = {},
    onNavToSettings: () -> Unit = {}
) {
    var markedStatus by remember { mutableStateOf(marked) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        ListItem(headlineContent = {
            Text(stringResource(R.string.save_image))
        }, leadingContent = {
            Icon(imageVector = Icons.Outlined.Save, null)
        }, modifier = Modifier.clickable {
            onDismissRequest()
            onSavePicture()
        })

        ListItem(headlineContent = {
            Text(stringResource(if (markedStatus) R.string.bookmark_remove else R.string.bookmark_add))
        }, leadingContent = {
            Icon(
                imageVector = if (markedStatus) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                null
            )
        }, modifier = Modifier.clickable {
            markedStatus = !markedStatus
            onBookMarkToggle()
        })

        ListItem(headlineContent = {
            Text(stringResource(R.string.jump_page))
        }, leadingContent = {
            Icon(imageVector = Icons.Outlined.Numbers, null)
        }, modifier = Modifier.clickable {
            onDismissRequest()
            onJumpPage()
        })

        Text(
            stringResource(R.string.read_mode),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        val pageEffects = LocalContext.current.resources.getStringArray(R.array.read_page_effects)
        var selectedIndex by remember { mutableIntStateOf(pageSwitchEffect) }

        LazyRow(
            Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            itemsIndexed(pageEffects) { index, label ->
                ToggleButton(
                    checked = selectedIndex == index,
                    onCheckedChange = {
                        selectedIndex = index
                        onPageSwitchEffectChange(index)
                    },
                    modifier = Modifier.semantics { role = Role.RadioButton },
                    shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        pageEffects.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                ) {
                    Text(label, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }

        ListItem(headlineContent = {
            Text(stringResource(R.string.menu_settings))
        }, leadingContent = {
            Icon(imageVector = Icons.Outlined.Settings, null)
        }, modifier = Modifier.clickable {
            onDismissRequest()
            onNavToSettings()
        })
    }
}