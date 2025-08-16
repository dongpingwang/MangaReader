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
import com.wolf2.reader.ui.common.MyLoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.ui.common.OnLifecycleEvent
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.read.component.CurlPageContent
import com.wolf2.reader.ui.common.ErrorIndicator
import com.wolf2.reader.ui.read.component.JumpPageDialog
import com.wolf2.reader.ui.read.component.MoreActionSheet
import com.wolf2.reader.ui.read.component.ReadBottomBar
import com.wolf2.reader.ui.read.component.ReadTopAppBar
import com.wolf2.reader.ui.read.component.VHPagerContent
import com.wolf2.reader.ui.read.component.SwipeTinderContent
import com.wolf2.reader.util.LoadResult

@Composable
fun ReadScreen(bookUuid: String) {
    val viewModel: ReadViewModel = viewModel(factory = ReadViewModel.provideFactory(bookUuid))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAppBar by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    val showSnackbar = uiState.snackbar != null
    var showJumpDialog by remember { mutableStateOf(false) }
    val curPage by viewModel.curPageFlow.collectAsStateWithLifecycle()

    OnLifecycleEvent(onDispose = {
        if (currentRoute()?.contains(Routes.IMAGE_PREVIEW) == true) return@OnLifecycleEvent
        if (currentRoute()?.contains(Routes.BOOK_DETAIL) == true) return@OnLifecycleEvent
        viewModel.onEvent(ReadUiEvent.OnDestroy)
    })

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState.bookResult) {
            is LoadResult.Loading -> MyLoadingIndicator()
            is LoadResult.Error -> ErrorIndicator()
            is LoadResult.Success<*> -> {
                when (PagerSwitchEffect.fromInt(uiState.pagerSwitchEffect)) {
                    PagerSwitchEffect.VerticalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = true,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar },
                    )

                    PagerSwitchEffect.HorizontalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = false,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar },
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
                chapterTitle = viewModel.getChapterTitle(),
                progressDesc = viewModel.getChapterProgress(),
                onBackHandle = {
                    viewModel.onEvent(ReadUiEvent.OnBackHandle)
                })
        }

        AnimatedVisibility(
            showAppBar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            ReadBottomBar(
                curPage = curPage,
                chapterRange = viewModel.getCurChapterPageRange(),
                onPrevChapter = { viewModel.onEvent(ReadUiEvent.OnPrevChapter) },
                onProgressChange = { viewModel.onEvent(ReadUiEvent.OnPageChange(it)) },
                onNextChapter = { viewModel.onEvent(ReadUiEvent.OnNextChapter) },
                onShowChapter = {},
                onMoreActionClick = {
                    showMoreSheet = true
                })
        }

        if (showMoreSheet) {
            MoreActionSheet(
                marked = viewModel.isCurPageBookMarked(),
                pageSwitchEffect = uiState.pagerSwitchEffect,
                onDismissRequest = { showMoreSheet = false },
                onSavePicture = { viewModel.onEvent(ReadUiEvent.OnCacheImage) },
                onBookMarkToggle = { viewModel.onEvent(ReadUiEvent.OnBookMarkToggle) },
                onJumpPage = { showJumpDialog = true },
                onPageSwitchEffectChange = {
                    viewModel.onEvent(
                        ReadUiEvent.OnPageSwitchEffectChange(
                            it
                        )
                    )
                }
            )
        }
        if (showSnackbar) {
            MySnackbar(uiState.snackbar!!)
        }
        if (showJumpDialog) {
            val book = (uiState.bookResult as LoadResult.Success).data
            JumpPageDialog(book.pageContents.size,
                onConfirmRequest = {
                    viewModel.onEvent(ReadUiEvent.OnPageChange(it))
                }, onDismissRequest = {
                    showJumpDialog = false
                })
        }
    }
}



