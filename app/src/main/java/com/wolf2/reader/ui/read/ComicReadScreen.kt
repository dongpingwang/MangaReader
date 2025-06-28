package com.wolf2.reader.ui.read

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.config.PagerSwitchEffect
import com.wolf2.reader.ui.common.LoadingIndicator
import com.wolf2.reader.ui.common.MySnackbar
import com.wolf2.reader.ui.common.OnLifecycleEvent
import com.wolf2.reader.ui.read.component.CurlPageContent
import com.wolf2.reader.ui.read.component.ErrorIndicator
import com.wolf2.reader.ui.read.component.ReadBottomContent
import com.wolf2.reader.ui.read.component.ReadTopAppBar
import com.wolf2.reader.ui.read.component.VHPagerContent
import com.wolf2.reader.ui.read.component.SwipeTinderContent
import com.wolf2.reader.util.LoadResult

@Composable
fun ReadScreen(bookUuid: String) {

    val viewModel: ReadViewModel = viewModel(factory = ReadViewModel.provideFactory(bookUuid))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAppBar by remember { mutableStateOf(false) }
    var showSnack = uiState.snackbar == true

    OnLifecycleEvent(onDispose = {
        viewModel.onEvent(ReadUiEvent.OnDestroy)
    })

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState.bookResult) {
            is LoadResult.Loading -> LoadingIndicator()
            is LoadResult.Error -> ErrorIndicator()
            is LoadResult.Success<*> -> {
                when (PagerSwitchEffect.fromInt(uiState.pagerSwitchEffect)) {
                    PagerSwitchEffect.VerticalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = true,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar }
                    )

                    PagerSwitchEffect.HorizontalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = false,
                        uiState = uiState,
                        onImageClick = { showAppBar = !showAppBar }
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

        if (showAppBar) {
            ReadTopAppBar(viewModel)
            ReadBottomContent(viewModel, uiState)
        }

        if (showSnack) {
            MySnackbar(
                message = stringResource(R.string.image_cache_success),
                actionLabel = stringResource(R.string.display_image_cache),
                withDismissAction = true,
                actionPerformed = {
                    viewModel.onEvent(ReadUiEvent.OnDisplayCacheImage)
                },
                dismissed = {
                    viewModel.onEvent(ReadUiEvent.OnSnackbarDismiss)
                })
        }
    }
}



