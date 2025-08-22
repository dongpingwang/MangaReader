package com.wolf2.reader.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.mode.entity.ReadTime
import com.wolf2.reader.ui.common.MyLoadingIndicator
import com.wolf2.reader.ui.history.component.HistoryList
import com.wolf2.reader.util.LoadResult

@Composable
fun HistoryScreen() {
    val vm: HistoryViewModel = viewModel(
        factory = HistoryViewModel.provideFactory()
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        when (uiState.readTimeResult) {
            is LoadResult.Loading -> MyLoadingIndicator()
            is LoadResult.Success -> {
                val result = (uiState.readTimeResult as LoadResult.Success<List<ReadTime>>).data
                HistoryList(
                    books = uiState.allBooks,
                    readTimes = result,
                    onNavigationToHome = { vm.onEvent(HistoryUiEvent.OnNavigationToHome) },
                    onNavigationToDetail = { vm.onEvent(HistoryUiEvent.OnNavigationToDetail(it)) })
            }

            else -> {}
        }
    }
}