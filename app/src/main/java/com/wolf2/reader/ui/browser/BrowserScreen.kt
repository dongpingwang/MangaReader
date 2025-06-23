package com.wolf2.reader.ui.browser

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.config.ebookMimeTypes
import com.wolf2.reader.ui.common.LoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.requestFullStorageAccess
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen() {
    val viewModel: BrowserViewModel = viewModel(
        factory = BrowserViewModel.provideFactory()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAccessDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        BrowserTopAppBar(viewModel = viewModel, uiState = uiState, onEmojiClick = {
            showAccessDialog = true
        })

        PickFilesContent(viewModel = viewModel, uiState = uiState, onShowAccessDialog = {
            showAccessDialog = true
        })

        if (showAccessDialog) {
            AccessDialog(
                uiState = uiState,
                onDismissRequest = { showAccessDialog = false })
        }

        PickerFilesIndicator(viewModel = viewModel, uiState = uiState)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(BrowserUiEvent.OnAccessChange)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserTopAppBar(
    viewModel: BrowserViewModel,
    uiState: BrowserUiState,
    onEmojiClick: () -> Unit = {}
) {
    TopAppBar(title = {

    }, navigationIcon = {
        IconButton(onClick = {
            viewModel.onEvent(BrowserUiEvent.OnBackHandle)
        }) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
        }
    }, actions = {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null
            )
        }
        IconButton(onClick = onEmojiClick) {
            Icon(
                imageVector = if (uiState.granted) Icons.Outlined.SentimentSatisfiedAlt else Icons.Outlined.SentimentDissatisfied,
                contentDescription = null
            )
        }
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Outlined.SdCard,
                contentDescription = null
            )
        }
    })
}

@Composable
private fun AccessDialog(
    uiState: BrowserUiState,
    onDismissRequest: () -> Unit = {}
) {

    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = {
            onDismissRequest()
            requestFullStorageAccess()
        }) {
            Text(stringResource(R.string.understand))
        }
    }, title = {
        Icon(
            imageVector = if (uiState.granted) Icons.Outlined.SentimentSatisfiedAlt else Icons.Outlined.SentimentVeryDissatisfied,
            contentDescription = null
        )
    }, text = {
        Text(stringResource(R.string.dialog_access_file_msg))
    })
}

@Composable
private fun PickerFilesIndicator(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    if (uiState.pickFileStatus == LoadResult.Loading) {
        LoadingIndicator()
    }
    if (uiState.snackbar == true) {
        MySnackbar(
            message = stringResource(R.string.pick_book_success),
            actionLabel = stringResource(R.string.to_book_shelf),
            withDismissAction = true,
            actionPerformed = {
                viewModel.onEvent(BrowserUiEvent.OnBackHandle)
            },
            dismissed = {
                viewModel.onEvent(BrowserUiEvent.OnSnackbarDismiss)
            })
    }
}

@Composable
private fun ColumnScope.PickFilesContent(
    viewModel: BrowserViewModel,
    uiState: BrowserUiState,
    onShowAccessDialog: () -> Unit = {}
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        Timber.d("picker files: $uris")
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        viewModel.onEvent(BrowserUiEvent.OnPickFiles(uris))
    }
    Box(
        modifier = Modifier
            .weight(1F)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = {
            if (uiState.granted) {
                pickerLauncher.launch(ebookMimeTypes.toTypedArray())
            } else {
                onShowAccessDialog()
            }
        }) {
            Text(stringResource(R.string.picker_book))
        }
    }
}