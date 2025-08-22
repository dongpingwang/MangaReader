package com.wolf2.reader.ui.browser

import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecyclePauseOrDisposeEffectResult
import androidx.lifecycle.compose.LifecycleResumeEffect
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
    val isLoading = uiState.pickFileStatus is LoadResult.Loading
    val showSnackbar = uiState.snackbar != null
    val showContent = !isLoading

    Box(modifier = Modifier.fillMaxSize()) {
        BrowserTopAppBar(
            onBackHandle = { viewModel.onEvent(BrowserUiEvent.OnBackHandle) },
            onAccessFileCheck = { requestFullStorageAccess() })

        if (showContent) {
            PickFilesContent(
                modifier = Modifier.align(Alignment.Center),
                isAccessFileGranted = uiState.granted,
                onPickFiles = { viewModel.onEvent(BrowserUiEvent.OnPickFiles(it)) },
                onAccessFileCheck = { requestFullStorageAccess() })
        }

        if (isLoading) {
            MyLoadingIndicator()
        }

        if (showSnackbar) {
            MySnackbar(uiState.snackbar!!)
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(BrowserUiEvent.OnAccessChange)
        object : LifecyclePauseOrDisposeEffectResult{
            override fun runPauseOrOnDisposeEffect() {
            }
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
private fun PickFilesContent(
    modifier: Modifier = Modifier,
    isAccessFileGranted: Boolean,
    onPickFiles: (List<Uri>) -> Unit,
    onAccessFileCheck: () -> Unit
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        Timber.d("picker files: $uris")
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        onPickFiles(uris)
    }
    TextButton(modifier = modifier, onClick = {
        if (!isAccessFileGranted) {
            onAccessFileCheck()
            return@TextButton
        }
        pickerLauncher.launch(ebookMimeTypes.toTypedArray())
    }) {
        Text(stringResource(R.string.picker_book))
    }
}