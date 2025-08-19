package com.wolf2.reader.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.ui.common.MyLoadingIndicator
import com.wolf2.reader.ui.detail.component.DetailContent
import com.wolf2.reader.ui.detail.component.DetailSheetContent
import com.wolf2.reader.ui.detail.component.DetailTopAppbar
import com.wolf2.reader.ui.common.ErrorIndicator
import com.wolf2.reader.util.LoadResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookUuid: String
) {
    val viewModel: DetailViewModel = viewModel(factory = DetailViewModel.provideFactory(bookUuid))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberStandardBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        sheetPeekHeight = 102.dp,
        scaffoldState = scaffoldState,
        sheetContent = {
            DetailSheetContent(
                onNavigationToRead = {
                    viewModel.onEvent(DetailUiEvent.OnNavigationToRead)
                    if (scaffoldState.bottomSheetState.hasExpandedState) {
                        scope.launch {
                            bottomSheetState.partialExpand()
                        }
                    }
                },
                onCopyContent = {
                    viewModel.onEvent(DetailUiEvent.OnCopyContent)
                },
                onDeleteReadRecord = {
                    viewModel.onEvent(DetailUiEvent.OnDeleteReadRecord)
                },
                onShareBookFile = {
                    viewModel.onEvent(DetailUiEvent.OnShareBookFile)
                }
            )
        }, topBar = {
            DetailTopAppbar(onBackHandle = {
                viewModel.onEvent(DetailUiEvent.OnBackHandle)
            })
        }, modifier = Modifier.systemBarsPadding()
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (uiState.bookResult) {
                is LoadResult.Loading -> MyLoadingIndicator()
                is LoadResult.Error -> ErrorIndicator()
                is LoadResult.Success<*> -> {
                    DetailContent(
                        uiState = uiState,
                        onTitleChange = {
                            viewModel.onEvent(DetailUiEvent.OnTitleChange(it))
                        },
                        onAuthorChange = {
                            viewModel.onEvent(DetailUiEvent.OnAuthorChange(it))
                        },
                        onFavoriteChange = {
                            viewModel.onEvent(DetailUiEvent.OnFavoriteChange(it))
                        })
                }
            }
        }
    }
}
