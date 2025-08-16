package com.wolf2.reader.ui.read

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.config.PagerSwitchEffect
import com.wolf2.reader.currentRoute
import com.wolf2.reader.ui.common.LoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.ui.common.OnLifecycleEvent
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.read.component.CurlPageContent
import com.wolf2.reader.ui.read.component.ErrorIndicator
import com.wolf2.reader.ui.read.component.ReadBottomContent
import com.wolf2.reader.ui.read.component.ReadDropMenu
import com.wolf2.reader.ui.read.component.ReadTopAppBar
import com.wolf2.reader.ui.read.component.VHPagerContent
import com.wolf2.reader.ui.read.component.SwipeTinderContent
import com.wolf2.reader.util.LoadResult
import timber.log.Timber

@Composable
fun ReadScreen(bookUuid: String) {

    val viewModel: ReadViewModel = viewModel(factory = ReadViewModel.provideFactory(bookUuid))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAppBar by remember { mutableStateOf(false) }
    var showMenuDrop by remember { mutableStateOf(false) }

    var marked by remember { mutableStateOf(false) }
    fun refreshMarkStatus(curPage: Int = viewModel.getCurPage()) {
        marked = uiState.bookMarks.find { it.pageIndex == curPage } != null
    }

    OnLifecycleEvent(onDispose = {
        if (currentRoute()?.contains(Routes.IMAGE_PREVIEW) == true) return@OnLifecycleEvent
        if (currentRoute()?.contains(Routes.BOOK_DETAIL) == true) return@OnLifecycleEvent
        viewModel.onEvent(ReadUiEvent.OnDestroy)
    })

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState.bookResult) {
            is LoadResult.Loading -> LoadingIndicator()
            is LoadResult.Error -> ErrorIndicator()
            is LoadResult.Success<*> -> {
                refreshMarkStatus(uiState.readRecord.curPage)
                when (PagerSwitchEffect.fromInt(uiState.pagerSwitchEffect)) {
                    PagerSwitchEffect.VerticalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = true,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar },
                        onPageChange = {
                            Timber.d(">>>onPageChange")
                            refreshMarkStatus(it)
                        }
                    )

                    PagerSwitchEffect.HorizontalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = false,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar },
                        onPageChange = { refreshMarkStatus(it) }
                    )

                    PagerSwitchEffect.CurlPage -> CurlPageContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar }
                    )

                    PagerSwitchEffect.VerticalList -> {}

                    PagerSwitchEffect.SwipeTinder -> {
                        SwipeTinderContent(vm = viewModel, uiState = uiState, onImageClick = {
                            showAppBar = !showAppBar
                        })
                    }
                }
            }
        }

        AnimatedVisibility(
            showAppBar,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ReadTopAppBar(
                marked = marked,
                onBackHandle = {
                    viewModel.onEvent(ReadUiEvent.OnBackHandle)
                }, onBookMarkToggle = {
                    viewModel.onEvent(ReadUiEvent.onBookMarkToggle)
                    // TODO 这里手动更新标题栏书签图标状态，不然Visibility变化才更新
                    marked = !marked
                },
                onShowDrop = {
                    showMenuDrop = true
                })
        }

        AnimatedVisibility(
            showAppBar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            ReadBottomContent(viewModel, uiState)
        }

        uiState.snackbar?.let {
            MySnackbar(it)
        }
        if (showMenuDrop) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                ReadDropMenu(readViewModel = viewModel, onDismissRequest = {
                    showMenuDrop = false
                })
            }
        }
    }
}



