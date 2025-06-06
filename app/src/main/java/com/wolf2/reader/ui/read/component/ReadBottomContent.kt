package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Toc
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wolf2.reader.R
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.read.ReadUiState
import com.wolf2.reader.ui.read.ReadViewModel
import com.wolf2.reader.util.LoadResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoxScope.ReadBottomContent(
    readViewModel: ReadViewModel,
    readUiState: ReadUiState
) {

    val uiState by readViewModel.uiState.collectAsStateWithLifecycle()
    val book = (uiState.bookResult as LoadResult.Success<Book>).data
    var showJumpPageDialog by remember { mutableStateOf(false) }
    var curSelectIndex by remember { mutableIntStateOf(-1) }

    Box(
        modifier = Modifier
            .align(Alignment.BottomStart),
        contentAlignment = Alignment.BottomStart,
    ) {
        NavigationBar() {
            NavigationBarItem(selected = curSelectIndex == 0, icon = {
                Icon(imageVector = Icons.Outlined.Toc, contentDescription = null)
            }, onClick = {
                curSelectIndex = 0
                globalViewContext?.navController?.navigate("${Routes.READ_CHAPTER}/${book.uuid}")
            })
            NavigationBarItem(selected = curSelectIndex == 1, icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_line_start_circle),
                    contentDescription = null
                )
            }, onClick = {
                curSelectIndex = 1
            })
            NavigationBarItem(selected = curSelectIndex == 2, icon = {
                Icon(
                    imageVector = if (uiState.darkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = null
                )
            }, onClick = {
                curSelectIndex = 2
                readViewModel.toggleDarkMode()
            })
            NavigationBarItem(selected = curSelectIndex == 3, icon = {
                Icon(imageVector = Icons.Outlined.Image, contentDescription = null)
            }, onClick = {
                curSelectIndex = 3
            })
        }

        if (curSelectIndex == 1) {
            ProgressAdjuster(readViewModel, uiState) { showJumpPageDialog = true }
        }
        if (curSelectIndex == 3) {
            PagerAdjuster(
                pageSwitchEffect = uiState.pagerSwitchEffect,
                onPageSwitchEffectChange = {
                    readViewModel.setPagerSwitchEffect(it)
                })
        }
    }
    if (showJumpPageDialog) {
        JumpPageDialog(pageCount = book.pageContents.size, onConfirmRequest = {
            readViewModel.updateReadRecord(it - 1, true)
        }, onDismissRequest = {
            showJumpPageDialog = false
        })
    }
}

@Composable
private fun ProgressAdjuster(
    readViewModel: ReadViewModel,
    readUiState: ReadUiState,
    onShowJumpPageDialog: () -> Unit = {}
) {
    val book = (readUiState.bookResult as LoadResult.Success<Book>).data
    Column(
        modifier = Modifier
            .padding(bottom = 80.dp)
            .background(NavigationBarDefaults.containerColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val _curPage = (readViewModel.getCurPage() + 1).toFloat()
        var sliderPosition by remember { mutableFloatStateOf(_curPage) }
        LaunchedEffect(readUiState) {
            snapshotFlow { readUiState.curPage }.collect {
                sliderPosition = (it + 1).toFloat()
            }
        }
        Row(modifier = Modifier.padding(top = 32.dp)) {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.KeyboardArrowLeft, contentDescription = null)
            }
            Slider(
                value = sliderPosition,
                valueRange = 1F..book.pageContents.size.toFloat(),
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    readViewModel.updateReadRecord(sliderPosition.toInt(), true)
                },
                modifier = Modifier.weight(1F)
            )
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        }
        TextButton(onClick = { onShowJumpPageDialog() }) {
            Text("${sliderPosition.toInt()}/${book.pageContents.size}")
        }
    }
}

@Composable
private fun PagerAdjuster(
    pageSwitchEffect: Int,
    onPageSwitchEffectChange: (Int) -> Unit = {},
) {
    val pageEffects = LocalContext.current.resources.getStringArray(R.array.read_page_effect)
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(bottom = 80.dp)
            .fillMaxWidth()
            .background(NavigationBarDefaults.containerColor)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(R.string.page_switch_effect),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        LazyRow() {

            itemsIndexed(pageEffects) { i, effect ->
                FilterChip(
                    selected = i == pageSwitchEffect,
                    onClick = {
                        onPageSwitchEffectChange(i)
                    },
                    label = {
                        Text(effect, maxLines = 1)
                    },
                    modifier = Modifier
                        .weight(1F)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}