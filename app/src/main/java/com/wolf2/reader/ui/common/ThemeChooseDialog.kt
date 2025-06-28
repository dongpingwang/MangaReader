package com.wolf2.reader.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R

@Composable
fun ChooseDialog(
    title: String,
    cur: Int,
    chooses: List<String>,
    onChooseChange: (Int) -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    var curOption by remember { mutableIntStateOf(cur) }

    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = {
            onDismissRequest()
            if (cur != curOption) {
                onChooseChange(curOption)
            }
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, title = {
        Text(title)
    }, text = {
        Column(Modifier.selectableGroup()) {
            chooses.forEachIndexed { i, text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = curOption == i,
                            onClick = {
                                curOption = i
                            },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = curOption == i,
                        onClick = null
                    )
                    Text(
                        text = text,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    })
}