package com.wolf2.reader.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.ui.home.component.HomeBottomBar
import com.wolf2.reader.ui.home.component.HomeContent
import com.wolf2.reader.ui.home.component.HomeDropMenu
import com.wolf2.reader.ui.home.component.HomeFab
import com.wolf2.reader.ui.home.component.HomeTopAppBar
import com.wolf2.reader.ui.home.component.ShelfLayoutModeDialog
import timber.log.Timber

@Composable
fun HomeScreen() {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory())
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showMenuDrop by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    val latestReadBook = vm.latestReadBookFlow.collectAsStateWithLifecycle(null)
    val showFab = latestReadBook.value != null

    Timber.d("latestReadBook: ${latestReadBook.value}")

    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopAppBar(
                curPageIndex = uiState.curTab,
                onNavigationToSearch = {
                    vm.onEvent(HomeUiEvent.OnNavigationToSearch)
                },
                onShowSortDialog = {},
                onShowMenuDialog = { showMenuDrop = true })
            HomeContent(uiState.curTab)
            HomeBottomBar(uiState, onTabChange = { vm.onEvent(HomeUiEvent.OnTabChange(it)) })
        }


        if (showFab) {
            HomeFab(
                onNavigationToRead = {
                    vm.onEvent(HomeUiEvent.OnNavigationToRead(requireNotNull(latestReadBook.value?.uuid)))
                }, modifier = Modifier
                    .systemBarsPadding()
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp)
                    .padding(end = 16.dp)
            )
        }
    }

    if (showMenuDrop) {
        HomeDropMenu(
            onNavigationToBrowser = { vm.onEvent(HomeUiEvent.OnNavigationToBrowser) },
            onNavigationToWifiTransfer = {},
            onShowLayoutDialog = { showLayoutDialog = true },
            onNavigationToSettings = { vm.onEvent(HomeUiEvent.OnNavigationToSettings) },
            onDismissRequest = { showMenuDrop = false })
    }

    if (showLayoutDialog) {
        ShelfLayoutModeDialog(
            layoutMode = uiState.shelfLayoutMode,
            layoutColumn = uiState.shelfLayoutColumn,
            onLayoutModeChange = { vm.onEvent(HomeUiEvent.OnShelfLayoutModeChange(it)) },
            onLayoutColumnChange = { vm.onEvent(HomeUiEvent.OnShelfLayoutColumnChange(it)) },
            onDismissRequest = { showLayoutDialog = false })
    }
}




