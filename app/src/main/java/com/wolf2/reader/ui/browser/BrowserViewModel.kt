package com.wolf2.reader.ui.browser

import android.net.Uri
import android.os.Environment
import androidx.compose.ui.util.fastForEach
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.convert.toBook
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.reader.LocalFileReader
import com.wolf2.reader.ui.browser.BrowserUiEvent.*
import com.wolf2.reader.util.LoadResult
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
    val pickFileStatus: LoadResult<Unit> = LoadResult.None
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
                            DocumentFile.fromSingleUri(globalContext, it) ?: return@fastForEach
                        val book = documentFile.toBook()
                        LocalFileReader(book).readBook()
                        newBooks.add(book)
                    }
                    _uiState.update { it.copy(pickFileStatus = LoadResult.Success(Unit)) }
                    if (newBooks.isEmpty()) return@launch
                    DatabaseHelper.bookDao().insertAll(newBooks)
                }
            }


            is OnBackHandle -> globalViewContext?.navController?.popBackStack()

            is OnAccessChange -> viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(granted = Environment.isExternalStorageManager()) }
            }

            is OnSnackbarDismiss -> viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(pickFileStatus = LoadResult.None) }
            }
        }
    }

}