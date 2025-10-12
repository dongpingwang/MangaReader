package com.wolf2.reader.ui.browser

import android.net.Uri
import androidx.compose.ui.util.fastForEach
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.getBasePath
import com.anggrayudi.storage.file.mimeType
import com.wolf2.reader.R
import com.wolf2.reader.config.EbookUtil
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.navigate
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.CachedReader
import com.wolf2.reader.reader.toBook
import com.wolf2.reader.ui.browser.BrowserUiEvent.*
import com.wolf2.reader.ui.common.SnackbarModel
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.takePersistableUriPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BrowserUiEvent {
    data class OnOpenDocumentTreeResult(val treeUri: Uri) : BrowserUiEvent()
    data object OnBackHandle : BrowserUiEvent()
    data object OnSnackbarDismiss : BrowserUiEvent()
    data object OnSafManage : BrowserUiEvent()
}

data class BrowserUiState(
    val pickFileStatus: LoadResult<Unit> = LoadResult.Success(Unit),
    val snackbar: SnackbarModel? = null
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

    fun onEvent(event: BrowserUiEvent) {
        when (event) {
            is OnOpenDocumentTreeResult -> loadBooks(event.treeUri)

            is OnBackHandle -> popBackStack()

            is OnSnackbarDismiss -> _uiState.update { it.copy(snackbar = null) }

            is OnSafManage -> navigate(Routes.SETTINGS_SAF)
        }
    }

    private fun filterEbookFile(source: List<DocumentFile>, result: MutableList<DocumentFile>) {
        source.fastForEach {
            if (it.isFile && it.canRead() && EbookUtil.isEbookMimeType(it.mimeType)) {
                result.add(it)
            }
            if (it.isDirectory && it.canRead()) {
                filterEbookFile(it.listFiles().toList(), result)
            }
        }
    }

    private fun loadBooks(treeUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            treeUri.takePersistableUriPermission()
            val tree = DocumentFileCompat.fromUri(globalContext, treeUri)
            val source = tree?.listFiles()?.toList()
            if (source.isNullOrEmpty()) {
                return@launch
            }
            _uiState.update { it.copy(pickFileStatus = LoadResult.Loading) }
            val result = mutableListOf<DocumentFile>()
            filterEbookFile(source = source, result = result)
            result.sortBy { it.getBasePath(globalContext) }
            val newBooks = mutableListOf<Book>()
            result.fastForEach { documentFile ->
                val uri = documentFile.uri
                val exists = DatabaseHelper.bookDao().queryByUri(uri) != null
                if (exists) return@fastForEach
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
                    snackbar = backSnackbar
                )
            }
            if (newBooks.isEmpty()) return@launch
            DatabaseHelper.bookDao().insertAll(newBooks)
        }
    }


    private val backSnackbar =
        SnackbarModel(
            message = globalContext.getString(R.string.pick_book_success),
            actionLabel = globalContext.getString(R.string.to_book_shelf),
            withDismissAction = true,
            actionPerformed = {
                onEvent(OnSnackbarDismiss)
                onEvent(OnBackHandle)
            },
            dismissed = { onEvent(OnSnackbarDismiss) }
        )

}