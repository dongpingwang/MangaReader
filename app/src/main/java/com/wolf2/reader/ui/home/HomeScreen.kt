package com.wolf2.reader.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.ui.home.component.HomeBottomBar
import com.wolf2.reader.ui.home.component.HomeContent
import com.wolf2.reader.ui.home.component.HomeDropMenu
import com.wolf2.reader.ui.home.component.HomeTopAppBar
import com.wolf2.reader.ui.home.component.ShelfLayoutModeDialog

@Composable
fun HomeScreen() {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory())
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showMenuDrop by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }

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
    if (showMenuDrop) {
        HomeDropMenu(
            onNavigationToBrowser = { vm.onEvent(HomeUiEvent.OnNavigationToBrowser) },
            onNavigationToWifiTransfer = {},
            onShowLayoutDialog = { showLayoutDialog = true },
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




