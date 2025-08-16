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
fun JumpPageDialog(
    pageCount: Int,
    onConfirmRequest: (Int) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    var pageInput by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = {
            val pageIndex = pageInput.toIntOrNull() ?: return@TextButton
            if (pageIndex !in 0..<pageCount) return@TextButton
            onConfirmRequest(pageIndex)
            onDismissRequest()
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, text = {
        TextField(
            value = pageInput,
            onValueChange = {
                pageInput = it.filter { it.isDigit() }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.focusRequester(focusRequester),
            placeholder = {
                Text(stringResource(R.string.hint_page_range, pageCount - 1))
            }
        )
    })
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}