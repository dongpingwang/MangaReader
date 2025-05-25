package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.wolf2.reader.R

@Composable
internal fun JumpPageDialog(
    pageCount: Int,
    onConfirmRequest: (Int) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    var pageInput by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = {
            val page = pageInput.toIntOrNull()
            page?.let { onConfirmRequest(it) }
            onDismissRequest()
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, title = {
        Text(stringResource(R.string.jump_the_page))
    }, text = {
        TextField(
            value = pageInput, onValueChange = {
                pageInput = it.filter { it.isDigit() }
            }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.focusRequester(focusRequester),
            placeholder = {
                if (pageCount > 1) {
                    Text(stringResource(R.string.hint_page_range, pageCount))
                }
            }
        )
    })
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}