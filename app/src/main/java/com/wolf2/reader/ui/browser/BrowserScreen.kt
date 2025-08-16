package com.wolf2.reader.ui.browser

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.config.ebookMimeTypes
import com.wolf2.reader.ui.common.MyLoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.requestFullStorageAccess
import timber.log.Timber

@Composable
fun BrowserScreen() {
    val viewModel: BrowserViewModel = viewModel(
        factory = BrowserViewModel.provideFactory()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        BrowserTopAppBar(
            onBackHandle = { viewModel.onEvent(BrowserUiEvent.OnBackHandle) },
            onAccessFileCheck = {
                requestFullStorageAccess()
            })

        PickFilesContent(
            modifier = Modifier.align(Alignment.Center),
            viewModel = viewModel,
            uiState = uiState,
            onAccessFileCheck = {
                requestFullStorageAccess()
            })

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
    onBackHandle: () -> Unit,
    onAccessFileCheck: () -> Unit
) {
    TopAppBar(title = {

    }, navigationIcon = {
        IconButton(onClick = onBackHandle) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
        }
    }, actions = {
        IconButton(onClick = onAccessFileCheck) {
            Icon(
                painter = painterResource(R.drawable.folder_managed),
                contentDescription = null
            )
        }
    })
}

@Composable
private fun PickerFilesIndicator(viewModel: BrowserViewModel, uiState: BrowserUiState) {
    if (uiState.pickFileStatus == LoadResult.Loading) {
        MyLoadingIndicator()
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
private fun PickFilesContent(
    modifier: Modifier = Modifier,
    viewModel: BrowserViewModel,
    uiState: BrowserUiState,
    onAccessFileCheck: () -> Unit = {}
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        Timber.d("picker files: $uris")
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        viewModel.onEvent(BrowserUiEvent.OnPickFiles(uris))
    }
    TextButton(modifier = modifier, onClick = {
        if (uiState.granted) {
            pickerLauncher.launch(ebookMimeTypes.toTypedArray())
        } else {
            onAccessFileCheck()
        }
    }) {
        Text(stringResource(R.string.picker_book))
    }
}