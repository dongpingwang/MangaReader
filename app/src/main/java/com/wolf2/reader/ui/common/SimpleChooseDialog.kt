package com.wolf2.reader.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed

@Composable
fun SimpleChooseDialog(
    curIndex: Int,
    options: List<String>,
    onDismissRequest: () -> Unit,
    onSelectedChange: (Int) -> Unit
) {

    var selectedIndex by remember { mutableIntStateOf(curIndex) }
    AlertDialog(onDismissRequest = onDismissRequest,
        confirmButton = {},
        text = {
            Column(Modifier.selectableGroup()) {
                options.fastForEachIndexed { i, text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (i == selectedIndex),
                                onClick = {
                                    selectedIndex = i
                                    onSelectedChange(i)
                                    onDismissRequest()
                                },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (i == selectedIndex),
                            onClick = null,
                        )
                        Text(
                            text = text,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        })
}