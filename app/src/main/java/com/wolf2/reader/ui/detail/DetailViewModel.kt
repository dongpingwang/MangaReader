package com.wolf2.reader.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.FavoriteBook
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.CachedReader
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.util.AppUtil
import com.wolf2.reader.util.ClipboardUtil
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.storagePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject
import timber.log.Timber

sealed class DetailUiEvent {
    data object OnBackHandle : DetailUiEvent()
    data class OnFavoriteChange(val favorite: Boolean) : DetailUiEvent()
    data class OnTitleChange(val title: String) : DetailUiEvent()
    data class OnAuthorChange(val author: String) : DetailUiEvent()
    data object OnNavigationToRead : DetailUiEvent()
    data object OnDeleteReadRecord : DetailUiEvent()
    data object OnShareBookFile : DetailUiEvent()
    data object OnCopyContent : DetailUiEvent()
    data object OnDestroy : DetailUiEvent()
}

data class DetailUiState(
    val bookResult: LoadResult<Book> = LoadResult.Loading,
    val favorite: Boolean = false,
    val readRecord: ReadRecord? = null
)

class DetailViewModel(val bookUuid: String) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(bookUuid: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DetailViewModel(bookUuid) as T
                }
            }
    }

    private var _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    private var fileReader: CachedReader? = null

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                queryBookData()
                readBookFile()
            }

            launch(Dispatchers.IO) {
                DatabaseHelper.bookDao().observeLatestReadBook().collectLatest {
                    onReadRecordChanged()
                }
            }
        }
    }


    private fun queryBookData() {
        val book = DatabaseHelper.bookDao().queryByUuid(bookUuid)
        if (book == null) {
            Timber.e("book from db is null")
            _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
            return
        }
        val readRecord = DatabaseHelper.readRecordDao().getReadRecord(bookUuid)
        val favorite = DatabaseHelper.favoriteBookDao().getFavoriteBook(bookUuid) != null
        _uiState.update {
            it.copy(
                bookResult = LoadResult.Success(book),
                readRecord = readRecord,
                favorite = favorite
            )
        }
    }

    private fun onReadRecordChanged() {
        val readRecord = DatabaseHelper.readRecordDao().getReadRecord(bookUuid)
        _uiState.update { it.copy(readRecord = readRecord) }
    }

    private fun readBookFile() {
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        val needUpdate = book.extraInfo.pageCount <= 0
        val reader = CachedReader.withLocalFileReader(book)
        fileReader = reader
        if (!reader.readBook(updateMetadata = false)) {
            Timber.e("readBook fail")
            _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
            return
        }
        if (needUpdate) {
            DatabaseHelper.bookDao().update(book)
        }
        _uiState.update { it.copy(bookResult = LoadResult.Success(book)) }
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.OnBackHandle -> popBackStack()

            is DetailUiEvent.OnFavoriteChange -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val favorite = FavoriteBook(bookUuid)
                    if (event.favorite) {
                        DatabaseHelper.favoriteBookDao().insert(favorite)
                    } else {
                        DatabaseHelper.favoriteBookDao().delete(favorite)
                    }
                }
            }

            is DetailUiEvent.OnTitleChange, is DetailUiEvent.OnAuthorChange -> {
                val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
                if (event is DetailUiEvent.OnTitleChange) {
                    book.title = event.title
                }
                if (event is DetailUiEvent.OnAuthorChange) {
                    book.author = event.author
                }
                viewModelScope.launch(Dispatchers.IO) {
                    DatabaseHelper.bookDao().update(book)
                }
            }

            is DetailUiEvent.OnNavigationToRead -> {
                navigate("${Routes.READ}/${bookUuid}")
            }

            is DetailUiEvent.OnDeleteReadRecord -> {
                val readRecord = _uiState.value.readRecord ?: return
                viewModelScope.launch(Dispatchers.IO) {
                    DatabaseHelper.readRecordDao().delete(readRecord)
                }
            }

            is DetailUiEvent.OnShareBookFile -> {
                val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
                val filePath = book.uri.storagePath()
                requireNotNull(filePath)
                AppUtil.shareFile(filePath, book.mimeType)
            }

            is DetailUiEvent.OnCopyContent -> {
                val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
                val readRecord = _uiState.value.readRecord
                val json = buildJsonObject {
                    put("书籍路径", book.uri.storagePath())
                    put("作者", book.author)
                    put(
                        "阅读进度",
                        if (readRecord == null) "None" else "${readRecord.curPage}/${readRecord.pageCount}"
                    )
                }.toString()
                val content = JSONObject(json).toString(2).replace("\\", "")
                ClipboardUtil.writeTo(content)
            }

            is DetailUiEvent.OnDestroy -> {
                fileReader?.close()
            }
        }
    }
}