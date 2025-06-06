package com.wolf2.reader.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.home.component.AnimatedIcon
import com.wolf2.reader.ui.home.component.HomeDropMenu
import com.wolf2.reader.ui.home.component.HomeTopAppBar
import com.wolf2.reader.ui.home.component.ShelfLayoutModeDialog
import com.wolf2.reader.ui.shelf.BookShelfScreen
import com.wolf2.reader.ui.setting.SettingScreen

@Composable
fun HomeScreen() {
    val vm: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(),
        viewModelStoreOwner = globalViewContext?.owner ?: LocalViewModelStoreOwner.current!!
    )
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


@Composable
private fun HomeBottomBar(uiState: HomeUiState, onTabChange: (Int) -> Unit = {}) {
    NavigationBar {
        var isSelected = uiState.curTab == 0
        NavigationBarItem(
            selected = isSelected,
            onClick = { onTabChange(0) },
            icon = {
                AnimatedIcon(
                    selected = isSelected,
                    selectedIcon = Icons.Filled.LibraryBooks,
                    unselectedIcon = Icons.Outlined.LibraryBooks
                )
            },
            alwaysShowLabel = false,
            label = {
                Text(stringResource(R.string.navi_book_shelf))
            })

        isSelected = uiState.curTab == 1
        NavigationBarItem(
            selected = isSelected,
            onClick = { onTabChange(1) },
            icon = {
                AnimatedIcon(
                    selected = isSelected,
                    selectedIcon = Icons.Filled.History,
                    unselectedIcon = Icons.Outlined.History
                )
            }, alwaysShowLabel = false, label = {
                Text(stringResource(R.string.navi_history))
            })
    }
}

@Composable
private fun ColumnScope.HomeContent(curTab: Int) {
    Box(modifier = Modifier.weight(1F)) {
        when (curTab) {
            0 -> BookShelfScreen()
            1 -> SettingScreen()
        }
    }
}


