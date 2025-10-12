package com.wolf2.reader.ui.browser

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var directoryAsBook = false
    var subDirectoryAsChapter = false

    Box(modifier = Modifier.fillMaxSize()) {
        BrowserTopAppBar(
            onBackHandle = { viewModel.onEvent(BrowserUiEvent.OnBackHandle) },
            onSafMange = { viewModel.onEvent(BrowserUiEvent.OnSafManage) })

        if (showContent) {
            PickFilesContent(
                modifier = Modifier.align(Alignment.Center),
                onOpenDocumentTreeResult = {
                    viewModel.onEvent(
                        BrowserUiEvent.OnOpenDocumentTreeResult(
                            it,
                            directoryAsBook,
                            subDirectoryAsChapter
                        )
                    )
                },
                onDirectoryAsBookChange = {
                    directoryAsBook = it
                },
                onSubDirectoryAsChapterChange = {
                    subDirectoryAsChapter = it
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
    onDirectoryAsBookChange: (Boolean) -> Unit,
    onSubDirectoryAsChapterChange: (Boolean) -> Unit,
) {
    var directoryAsBook by remember { mutableStateOf(false) }
    var subDirectoryAsChapter by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        Timber.d("OpenDocumentTree: $uri")
        uri ?: return@rememberLauncherForActivityResult
        onOpenDocumentTreeResult(uri)
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = {
            pickerLauncher.launch(null)
        }) {
            Text(stringResource(R.string.picker_book))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = directoryAsBook, onCheckedChange = {
                directoryAsBook = it
                onDirectoryAsBookChange(it)
            })
            Text(
                stringResource(R.string.directory_as_a_book),
                style = MaterialTheme.typography.bodySmall
            )
        }
        AnimatedVisibility(directoryAsBook) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = subDirectoryAsChapter, onCheckedChange = {
                    subDirectoryAsChapter = it
                    onSubDirectoryAsChapterChange(it)
                })
                Text(
                    stringResource(R.string.directory_as_a_book_chapter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}