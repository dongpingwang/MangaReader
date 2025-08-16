package com.wolf2.reader.ui.browser

import android.net.Uri
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.mimeType
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.reader.toBook
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.CachedReader
import com.wolf2.reader.ui.browser.BrowserUiEvent.*
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.isExternalStorageManager
import com.wolf2.reader.util.takePersistableUriPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BrowserUiEvent {
    data class OnPickFiles(val uris: List<Uri>) : BrowserUiEvent()
    data object OnBackHandle : BrowserUiEvent()
    data object OnAccessChange : BrowserUiEvent()
    data object OnSnackbarDismiss : BrowserUiEvent()
}

data class BrowserUiState(
    val granted: Boolean = false,
    val pickFileStatus: LoadResult<Unit> = LoadResult.Success(Unit),
    val snackbar: Boolean? = null
)

class BrowserViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BrowserViewModel() as T
            }
        }
    }

    init {
        onEvent(OnAccessChange)
    }

    fun onEvent(event: BrowserUiEvent) {
        when (event) {
            is OnPickFiles -> {
                viewModelScope.launch(Dispatchers.IO) {
                    _uiState.update { it.copy(pickFileStatus = LoadResult.Loading) }
                    val newBooks = mutableListOf<Book>()
                    event.uris.fastForEach {
                        it.takePersistableUriPermission()
                        val exists = DatabaseHelper.bookDao().queryByUri(it) != null
                        if (exists == true) return@fastForEach
                        val documentFile =
                            DocumentFileCompat.fromUri(globalContext, it) ?: return@fastForEach
                        val book = documentFile.toBook()
                        CachedReader.newLocalFileReader(book).apply {
                            readBook(updatePageContent = false)
                            close()
                        }
                        newBooks.add(book)
                    }
                    _uiState.update {
                        it.copy(
                            pickFileStatus = LoadResult.Success(Unit),
                            snackbar = true
                        )
                    }
                    if (newBooks.isEmpty()) return@launch
                    DatabaseHelper.bookDao().insertAll(newBooks)
                }
            }

            is OnBackHandle -> popBackStack()

            is OnAccessChange -> viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(granted = isExternalStorageManager()) }
            }

            is OnSnackbarDismiss -> viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(snackbar = null) }
            }
        }
    }

}