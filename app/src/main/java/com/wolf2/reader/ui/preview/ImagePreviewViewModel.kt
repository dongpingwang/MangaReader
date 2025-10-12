package com.wolf2.reader.ui.preview

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wolf2.reader.config.MemoryGlobal
import com.wolf2.reader.popBackStack
import com.wolf2.reader.util.AppUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ImagePreviewUiState(val imageUri: Uri)

sealed class ImagePreviewUiEvent {
    data object OnBackHandle : ImagePreviewUiEvent()
    data object OnShare : ImagePreviewUiEvent()
}

class ImagePreviewViewModel() : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ImagePreviewViewModel() as T
                }
            }
    }

    private var _uiState =
        MutableStateFlow(ImagePreviewUiState(imageUri = MemoryGlobal.cacheImageUri!!))
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ImagePreviewUiEvent) {
        when (event) {
            is ImagePreviewUiEvent.OnBackHandle -> popBackStack()

            is ImagePreviewUiEvent.OnShare -> {
                AppUtil.shareFile(uri = _uiState.value.imageUri, fileType = "image/*")
            }
        }
    }
}