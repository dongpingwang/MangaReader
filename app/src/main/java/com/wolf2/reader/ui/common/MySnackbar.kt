package com.wolf2.reader.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MySnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short,
    actionPerformed: () -> Unit = {},
    dismissed: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        SnackbarHost(hostState = snackbarHostState)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration
            )
            when (result) {
                SnackbarResult.ActionPerformed -> actionPerformed()
                SnackbarResult.Dismissed -> dismissed()
            }
        }
    }
}