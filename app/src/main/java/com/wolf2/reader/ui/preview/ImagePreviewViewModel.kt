package com.wolf2.reader.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wolf2.reader.popBackStack
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.AppUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ImagePreviewUiState(val imgPath: String)

sealed class ImagePreviewUiEvent {
    data object OnBackHandle : ImagePreviewUiEvent()
    data object OnShare : ImagePreviewUiEvent()
}

class ImagePreviewViewModel(imgName: String) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(imgName: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ImagePreviewViewModel(imgName) as T
                }
            }
    }

    private var _uiState =
        MutableStateFlow(ImagePreviewUiState(imgPath = ImageCacheUtil.getCacheImageDiskPath(imgName)))
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ImagePreviewUiEvent) {
        when (event) {
            is ImagePreviewUiEvent.OnBackHandle -> popBackStack()

            is ImagePreviewUiEvent.OnShare -> {
                AppUtil.shareFile(path = _uiState.value.imgPath, fileType = "image/*")
            }
        }
    }
}