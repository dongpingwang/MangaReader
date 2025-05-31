package com.wolf2.reader.ui.read

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.config.PageSwitchEffect
import com.wolf2.reader.ui.common.LoadingIndicator
import com.wolf2.reader.ui.read.component.CurlPageContent
import com.wolf2.reader.ui.read.component.ErrorIndicator
import com.wolf2.reader.ui.read.component.ReadBottomContent
import com.wolf2.reader.ui.read.component.ReadTopAppBar
import com.wolf2.reader.ui.read.component.VHPagerContent
import com.wolf2.reader.util.LoadResult

@Composable
fun ReadScreen(bookUuid: String) {

    val viewModel: ReadViewModel = viewModel(factory = ReadViewModel.provideFactory(bookUuid))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBar by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState.bookResult) {
            is LoadResult.Loading -> LoadingIndicator()
            is LoadResult.Error -> ErrorIndicator()
            is LoadResult.Success<*> -> {
                when (uiState.pagerSwitchEffect) {
                    PageSwitchEffect.VerticalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = true,
                        uiState = uiState,
                        onImageClick = { showBar = !showBar }
                    )
                    PageSwitchEffect.HorizontalPage -> VHPagerContent(
                        videModel = viewModel,
                        isVerticalPager = false,
                        uiState = uiState,
                        onImageClick = { showBar = !showBar }
                    )
                    PageSwitchEffect.CurlPage -> CurlPageContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        onImageClick = { showBar = !showBar }
                    )
                }
            }
            else -> {}
        }

        if (showBar) {
            ReadTopAppBar(viewModel)
            ReadBottomContent(viewModel, uiState)
        }
    }
}



