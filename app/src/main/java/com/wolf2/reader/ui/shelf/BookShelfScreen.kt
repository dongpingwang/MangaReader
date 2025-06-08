package com.wolf2.reader.ui.shelf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.wolf2.reader.R
import com.wolf2.reader.config.Constants
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.home.HomeUiState
import com.wolf2.reader.ui.home.HomeViewModel
import com.wolf2.reader.ui.shelf.component.ShelfGridContent
import com.wolf2.reader.ui.shelf.component.ShelfListContent

@Composable
fun BookShelfScreen() {
    val vm: BookShelfViewModel = viewModel(
        factory = BookShelfViewModel.provideFactory(),
        viewModelStoreOwner = globalViewContext?.owner ?: LocalViewModelStoreOwner.current!!
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val homeVM: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory()
    )

    val homeUiState by homeVM.uiState.collectAsStateWithLifecycle()

    val pager = remember { vm.allBooksPager }
    val bookPagingItems = pager.flow.collectAsLazyPagingItems()


    when {
        bookPagingItems.itemCount <= 0 -> EmptyHint {
            vm.onEvent(
                BookShelfUiEvent.OnNavigationToBrowser
            )
        }
        else -> {
            ShelfContent(
                uiState = uiState,
                bookPagingItems = bookPagingItems,
                homeUiState = homeUiState,
                vm = vm
            )
        }
    }
}

@Composable
private fun ShelfContent(
    uiState: BookShelfUiState,
    bookPagingItems: LazyPagingItems<Book>,
    homeUiState: HomeUiState,
    vm: BookShelfViewModel
) {
    when (homeUiState.shelfLayoutMode) {
        Constants.List -> ShelfListContent(
            uiState = uiState,
            bookPagingItems = bookPagingItems,
            onItemClick = {
                vm.onEvent(BookShelfUiEvent.OnItemClick(it))
            },
            onItemLongClick = {
                vm.onEvent(BookShelfUiEvent.OnItemLongClick(it))
            }
        )

        Constants.Grid -> ShelfGridContent(
            uiState = uiState,
            bookPagingItems = bookPagingItems,
            column = homeUiState.shelfLayoutColumn,
            onItemClick = {
                vm.onEvent(BookShelfUiEvent.OnItemClick(it))
            },
            onItemLongClick = {
                vm.onEvent(BookShelfUiEvent.OnItemLongClick(it))
            }
        )
    }
}

@Composable
private fun EmptyHint(onNavigationToBrowser: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TextButton(onClick = onNavigationToBrowser) {
            Text(text = stringResource(R.string.btn_browser_book))
        }
    }
}