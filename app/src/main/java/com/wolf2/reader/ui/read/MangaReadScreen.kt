package com.wolf2.reader.ui.read

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LifecycleStopOrDisposeEffectResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.wolf2.reader.config.PagerSwitchEffect
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.MyLoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.ui.read.component.CurlPager
import com.wolf2.reader.ui.common.ErrorIndicator
import com.wolf2.reader.ui.read.component.ChapterSheet
import com.wolf2.reader.ui.read.component.JumpPageDialog
import com.wolf2.reader.ui.read.component.MoreActionSheet
import com.wolf2.reader.ui.read.component.RVPager
import com.wolf2.reader.ui.read.component.ReadBottomBar
import com.wolf2.reader.ui.read.component.ReadTopAppBar
import com.wolf2.reader.ui.read.component.VHPager
import com.wolf2.reader.ui.read.component.TinderPager
import com.wolf2.reader.util.LoadResult

@Composable
fun MangaReadScreen(bookUuid: String, from: String, curPage: Int?) {
    val viewModel: ReadViewModel =
        viewModel(factory = ReadViewModel.provideFactory(bookUuid, from, curPage))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAppBar by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    val showSnackbar = uiState.snackbar != null
    var showJumpDialog by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    val curPage by viewModel.curPageFlow.collectAsStateWithLifecycle()
    val systemUiController = rememberSystemUiController()

    fun toggleBarsVisibility() {
        showAppBar = !showAppBar
    }

    systemUiController.isSystemBarsVisible = uiState.fullScreen.not()
    LifecycleStartEffect(Unit) {
        systemUiController.isSystemBarsVisible = uiState.fullScreen.not()
        viewModel.onEvent(ReadUiEvent.OnLifecycleStart)
        object : LifecycleStopOrDisposeEffectResult {
            override fun runStopOrDisposeEffect() {
                systemUiController.isSystemBarsVisible = true
                viewModel.onEvent(ReadUiEvent.OnLifecycleStop)
            }
        }
    }

    fun updateReadRecord(pageInt: Int, updateImmediately: Boolean) {
        viewModel.onEvent(ReadUiEvent.OnPageChange(pageInt, updateImmediately))
    }

    fun loadBuffer(pageContent: PageContent): ByteArray? {
        return viewModel.getImageBuffer(pageContent)
    }

    fun loadImage(pageContent: PageContent): Bitmap {
        return viewModel.loadImage(pageContent)
    }

    var modifier = Modifier.fillMaxSize()
    if (uiState.fullScreen) {
        modifier = modifier.systemBarsPadding()
    }

    Box(
        modifier = modifier
    ) {
        when (uiState.bookResult) {
            is LoadResult.Loading -> MyLoadingIndicator()
            is LoadResult.Error -> ErrorIndicator()
            is LoadResult.Success<*> -> {
                val book = (uiState.bookResult as LoadResult.Success<Book>).data
                val pagerEffect: PagerSwitchEffect =
                    PagerSwitchEffect.fromInt(uiState.pagerSwitchEffect)
                when (pagerEffect) {
                    PagerSwitchEffect.VerticalPage, PagerSwitchEffect.HorizontalPage -> VHPager(
                        isDarkMode = uiState.darkMode,
                        isVerticalPager = pagerEffect == PagerSwitchEffect.VerticalPage,
                        curPage = uiState.curPage,
                        pageContents = book.pageContents,
                        onLoadBuffer = {
                            loadBuffer(it)
                        }, onImageClick = {
                            toggleBarsVisibility()
                        }, onPageChange = {
                            updateReadRecord(it, false)
                        })

                    PagerSwitchEffect.CurlPage -> CurlPager(
                        isDarkMode = uiState.darkMode,
                        curPage = uiState.curPage,
                        pageContents = book.pageContents,
                        onLoadImage = {
                            loadImage(it)
                        }, onImageClick = {
                            toggleBarsVisibility()
                        }, onPageChange = {
                            updateReadRecord(it, false)
                        })


                    PagerSwitchEffect.VerticalList -> {
                        RVPager(
                            isDarkMode = uiState.darkMode,
                            curPage = uiState.curPage,
                            pageContents = book.pageContents,
                            onLoadImage = {
                                loadImage(it)
                            }, onImageClick = {
                                toggleBarsVisibility()
                            }, onPageChange = {
                                updateReadRecord(it, false)
                            })
                    }

                    PagerSwitchEffect.SwipeTinder -> {
                        TinderPager(
                            isDarkMode = uiState.darkMode,
                            curPage = uiState.curPage,
                            pageContents = book.pageContents,
                            onLoadImage = {
                                loadImage(it)
                            }, onImageClick = {
                                toggleBarsVisibility()
                            }, onPageChange = {
                                updateReadRecord(it, false)
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
                onProgressChange = { updateReadRecord(it, true) },
                onNextChapter = { viewModel.onEvent(ReadUiEvent.OnNextChapter) },
                onShowChapter = { showChapterSheet = true },
                onMoreActionClick = { showMoreSheet = true })
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
                    viewModel.onEvent(ReadUiEvent.OnPageSwitchEffectChange(it))
                },
                onNavToSettings = {
                    viewModel.onEvent(ReadUiEvent.OnNavigationToSettings)
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
                    updateReadRecord(it, true)
                }, onDismissRequest = {
                    showJumpDialog = false
                })
        }
        if (showChapterSheet) {
            val book = (uiState.bookResult as LoadResult.Success).data
            ChapterSheet(
                book = book,
                bookMarks = uiState.bookMarks,
                onDismissRequest = { showChapterSheet = false },
                onPageChange = {
                    showChapterSheet = false
                    updateReadRecord(it, true)
                },
                onLoadImage = { loadImage(it) }
            )
        }
    }
}



