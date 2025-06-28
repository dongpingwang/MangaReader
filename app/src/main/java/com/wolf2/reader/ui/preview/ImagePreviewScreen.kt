package com.wolf2.reader.ui.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R
import com.wolf2.reader.ui.read.component.PageAsyncImage
import me.saket.cascade.CascadeDropdownMenu
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(imgName: String) {

    val viewModel: ImagePreviewViewModel =
        viewModel(factory = ImagePreviewViewModel.provideFactory(imgName))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAppBar by remember { mutableStateOf(false) }
    var showDropMenu by remember { mutableStateOf(false) }

    val insets = WindowInsets.statusBars
    val statusBarHeight = with(LocalDensity.current) {
        insets.getTop(this).toDp()
    }

    val modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .zoomable(
            zoomState = rememberZoomState(),
            onTap = {
                showAppBar = !showAppBar
            })

    Scaffold(topBar = {
        if (showDropMenu) {
            CascadeDropdownMenu(
                expanded = true,
                offset = DpOffset(LocalConfiguration.current.screenWidthDp.dp, -statusBarHeight),
                onDismissRequest = {
                    showDropMenu = false
                }) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.sbl_menu_share)) },
                    onClick = {
                        showDropMenu = false
                        viewModel.onEvent(ImagePreviewUiEvent.OnShare)
                    }
                )
            }
        }

        AnimatedVisibility(
            showAppBar,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(onClick = {
                    viewModel.onEvent(ImagePreviewUiEvent.OnBackHandle)
                }) {
                    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
                }
            }, actions = {
                IconButton(onClick = {
                    showDropMenu = true
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert, contentDescription = null
                    )
                }
            })
        }
    }) { innerPadding ->
        PageAsyncImage(model = uiState.imgPath, modifier = modifier)
    }
}