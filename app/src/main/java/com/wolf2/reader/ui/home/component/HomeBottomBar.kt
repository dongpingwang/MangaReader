package com.wolf2.reader.ui.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wolf2.reader.R
import com.wolf2.reader.constant.NavLabelShow
import com.wolf2.reader.ui.home.HomeUiState

@Composable
fun HomeBottomBar(uiState: HomeUiState, onTabChange: (Int) -> Unit = {}) {
    val alwaysShowLabel = NavLabelShow.fromInt(uiState.navLabelShow) == NavLabelShow.AlwaysShow
    val showLabel = NavLabelShow.fromInt(uiState.navLabelShow) != NavLabelShow.Hidden
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
            alwaysShowLabel = alwaysShowLabel,
            label = {
                if (!showLabel) return@NavigationBarItem
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
            }, alwaysShowLabel = alwaysShowLabel,
            label = {
                if (!showLabel) return@NavigationBarItem
                Text(stringResource(R.string.navi_history))
            })
    }
}
