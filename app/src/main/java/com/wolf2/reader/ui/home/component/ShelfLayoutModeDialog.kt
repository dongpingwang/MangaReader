package com.wolf2.reader.ui.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R

@Composable
internal fun ShelfLayoutModeDialog(
    layoutMode: Int,
    layoutColumn: Int,
    onDismissRequest: () -> Unit = {},
    onLayoutModeChange: (Int) -> Unit = {},
    onLayoutColumnChange: (Int) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(R.string.menu_layout_edit))
        },
        text = {
            val layoutOptions = stringArrayResource(R.array.shelf_layout_options)

            var sliderPosition by remember {
                mutableFloatStateOf(layoutColumn.toFloat())
            }
            Column() {
                Text(stringResource(R.string.shelf_layout_mode))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    layoutOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = layoutOptions.size
                            ),
                            onClick = {
                                onLayoutModeChange(index)
                            },
                            selected = layoutMode == index,
                            label = { Text(label) }
                        )
                    }
                }
                Text(stringResource(R.string.shelf_grid_column))
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        onLayoutColumnChange(it.toInt())
                    },
                    steps = 1,
                    valueRange = 2f..4f
                )
            }

        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(stringResource(R.string.confirm))
            }
        })
}