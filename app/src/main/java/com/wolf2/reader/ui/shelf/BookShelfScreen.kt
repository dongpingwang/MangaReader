package com.wolf2.reader.ui.shelf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.wolf2.reader.R
import com.wolf2.reader.config.ShelfLayout
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.common.EmptyHint
import com.wolf2.reader.ui.home.HomeUiState
import com.wolf2.reader.ui.home.HomeViewModel
import com.wolf2.reader.ui.shelf.component.ShelfGridContent
import com.wolf2.reader.ui.shelf.component.ShelfListContent

@Composable
fun BookShelfScreen() {
    val vm: BookShelfViewModel = viewModel(
        factory = BookShelfViewModel.provideFactory()
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val homeVM: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory()
    )
    val homeUiState by homeVM.uiState.collectAsStateWithLifecycle()
    val pager = vm.getPager(uiState.curSort, uiState.curFilter)
    val bookPagingItems = pager.flow.collectAsLazyPagingItems()

    when {
        bookPagingItems.itemCount <= 0 -> EmptyHint(hint = stringResource(R.string.btn_browser_book)) {
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
    when (ShelfLayout.fromInt(homeUiState.shelfLayoutMode)) {
        ShelfLayout.LIST -> ShelfListContent(
            uiState = uiState,
            bookPagingItems = bookPagingItems,
            onItemClick = {
                vm.onEvent(BookShelfUiEvent.OnItemClick(it))
            },
            onItemLongClick = {
                vm.onEvent(BookShelfUiEvent.OnItemLongClick(it))
            }
        )

        ShelfLayout.GIRD -> ShelfGridContent(
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