package com.wolf2.reader.ui.common.chapter

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeadButtons(
    selectedIndex: Int,
    onSelectChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    var curIndex by remember { mutableIntStateOf(selectedIndex) }
    val scope = rememberCoroutineScope()
    scope.launch { curIndex = selectedIndex }

    Row(modifier = modifier) {
        ToggleButton(
            checked = curIndex == 0,
            onCheckedChange = {
                curIndex = 0
                onSelectChange(0)
            },
            modifier = Modifier
                .semantics { role = Role.RadioButton }
                .padding(horizontal = 2.dp),
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
        ) {
            Icon(imageVector = Icons.Outlined.FormatListBulleted, contentDescription = null)
        }

        ToggleButton(
            checked = curIndex == 1,
            onCheckedChange = {
                curIndex = 1
                onSelectChange(1)
            },
            modifier = Modifier
                .semantics { role = Role.RadioButton }
                .padding(horizontal = 2.dp),
            shapes = ButtonGroupDefaults.connectedMiddleButtonShapes()
        ) {
            Icon(imageVector = Icons.Outlined.GridView, contentDescription = null)
        }

        ToggleButton(
            checked = curIndex == 2,
            onCheckedChange = {
                curIndex = 2
                onSelectChange(2)
            },
            modifier = Modifier
                .semantics { role = Role.RadioButton }
                .padding(horizontal = 2.dp),
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
        ) {
            Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null)
        }
    }
}