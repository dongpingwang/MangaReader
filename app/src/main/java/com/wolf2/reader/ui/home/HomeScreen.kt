package com.wolf2.reader.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewQuilt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.ui.shelf.BookShelfScreen
import com.wolf2.reader.ui.setting.SettingScreen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column {
        HomeTopAppBar(uiState, viewModel)
        HomeContent(uiState)
        HomeBottomBar(uiState, viewModel)
    }
}

@Composable
private fun HomeDropMenu(
    expanded: Boolean = false,
    onNavigationToBrowser: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = ShapeDefaults.Medium
    ) {
        DropdownMenuItem(text = { Text(stringResource(R.string.menu_pick_file)) }, leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.CreateNewFolder,
                contentDescription = null
            )
        }, onClick = {
            onNavigationToBrowser()
            onDismissRequest()
        })
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_wifi_transfer)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = null
                )
            },
            onClick = {
                onDismissRequest()
            })
        DropdownMenuItem(text = { Text(stringResource(R.string.menu_shelf_edit)) }, leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_arrange),
                contentDescription = null
            )
        }, onClick = {
            onDismissRequest()
        })

        DropdownMenuItem(text = { Text(stringResource(R.string.menu_layout_edit)) }, leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.ViewQuilt,
                contentDescription = null
            )
        }, onClick = {
            onDismissRequest()
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(uiState: HomeUiState, viewModel: HomeViewModel) {
    val curPageIndex = uiState.curTab
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(title = {
        Text(text = stringResource(if (curPageIndex == 0) R.string.navi_book_shelf else R.string.navi_setting))
    }, actions = {

        HomeDropMenu(
            expanded = expanded,
            onNavigationToBrowser = { viewModel.onEvent(HomeUiEvent.OnNavigationToBrowser) },
            onDismissRequest = { expanded = false })

        AnimatedVisibility(curPageIndex == 0) {
            IconButton(onClick = { viewModel.onEvent(HomeUiEvent.OnNavigationToSearch) }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            }
        }
        AnimatedVisibility(curPageIndex == 0) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = null
                )
            }
        }
    })
}

@Composable
private fun HomeBottomBar(uiState: HomeUiState, homeViewModel: HomeViewModel) {
    NavigationBar {
        var isSelected = uiState.curTab == 0
        NavigationBarItem(
            selected = isSelected,
            onClick = { homeViewModel.onEvent(HomeUiEvent.OnTabChange(0)) },
            icon = {
                BadgedBox(badge = {
                    if (!uiState.showBadge) return@BadgedBox
                    if (uiState.readingCount <= 0) return@BadgedBox
                    Badge(
                        contentColor = Color.White,
                        containerColor = Color.Red
                    ) {
                        Text(text = uiState.readingCount.toString())
                    }
                }) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = null,
                    )
                }
            },
            alwaysShowLabel = false,
            label = {
                Text(stringResource(R.string.navi_book_shelf))
            })

        isSelected = uiState.curTab == 1
        NavigationBarItem(
            selected = isSelected,
            onClick = { homeViewModel.onEvent(HomeUiEvent.OnTabChange(1)) },
            icon = {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = null,
                )
            }, alwaysShowLabel = false, label = {
                Text(stringResource(R.string.navi_setting))
            })
    }
}

@Composable
private fun ColumnScope.HomeContent(uiState: HomeUiState) {
    val pagerState = rememberPagerState() { 2 }
    val scope = rememberCoroutineScope()
    scope.launch {
        pagerState.animateScrollToPage(uiState.curTab)
    }
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false,
        modifier = Modifier.weight(1F)
    ) {
        when (it) {
            0 -> BookShelfScreen()
            1 -> SettingScreen()
        }
    }
}