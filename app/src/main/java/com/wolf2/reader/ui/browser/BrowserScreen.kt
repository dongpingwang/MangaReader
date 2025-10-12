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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.ui.common.MyLoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.util.LoadResult
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
            onSafMange = { viewModel.onEvent(BrowserUiEvent.OnSafManage) })

        if (showContent) {
            PickFilesContent(
                modifier = Modifier.align(Alignment.Center),
                onOpenDocumentTreeResult = {
                    viewModel.onEvent(
                        BrowserUiEvent.OnOpenDocumentTreeResult(it)
                    )
                },
            )
        }

        if (isLoading) {
            MyLoadingIndicator()
        }

        if (showSnackbar) {
            MySnackbar(uiState.snackbar!!)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserTopAppBar(
    onBackHandle: () -> Unit,
    onSafMange: () -> Unit
) {
    TopAppBar(title = {

    }, navigationIcon = {
        IconButton(onClick = onBackHandle) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
        }
    }, actions = {
        IconButton(onClick = onSafMange) {
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
    onOpenDocumentTreeResult: (Uri) -> Unit,
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        Timber.d("OpenDocumentTree: $uri")
        uri ?: return@rememberLauncherForActivityResult
        onOpenDocumentTreeResult(uri)
    }
    TextButton(modifier = modifier, onClick = {
        pickerLauncher.launch(null)
    }) {
        Text(stringResource(R.string.picker_book))
    }
}