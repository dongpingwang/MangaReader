package com.wolf2.reader.ui.detail

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.R
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.BookMark
import com.wolf2.reader.mode.entity.FavoriteBook
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.CachedReader
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.util.AppUtil
import com.wolf2.reader.util.ClipboardUtil
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.globalContext
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
    data class OnNavigationToRead(val curPage: Int = -1) : DetailUiEvent()
    data object OnDeleteReadRecord : DetailUiEvent()
    data object OnShareBookFile : DetailUiEvent()
    data object OnCopyContent : DetailUiEvent()
}

data class DetailUiState(
    val bookResult: LoadResult<Book> = LoadResult.Loading,
    val favorite: Boolean = false,
    val readRecord: ReadRecord? = null,
    val bookMarks: MutableList<BookMark> = mutableListOf(),
    val updateTimeMillis: Long = System.currentTimeMillis()
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

    override fun onCleared() {
        super.onCleared()
        fileReader?.close()
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
        val bookMarks = DatabaseHelper.bookMarkDao().queryByBookUuid(bookUuid).toMutableList()
        _uiState.update {
            it.copy(
                bookResult = LoadResult.Success(book),
                readRecord = readRecord,
                favorite = favorite,
                bookMarks = bookMarks,
                updateTimeMillis = System.currentTimeMillis()
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
        _uiState.update {
            it.copy(
                bookResult = LoadResult.Success(book),
                updateTimeMillis = System.currentTimeMillis()
            )
        }
    }

    fun getImageBuffer(page: PageContent): ByteArray? {
        return fileReader?.getImageBuffer(page)
    }

    fun getImageBitmap(page: PageContent): Bitmap? {
        return fileReader?.getImageBitmap(page)
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.OnBackHandle -> popBackStack()

            is DetailUiEvent.OnFavoriteChange -> updateFavorite(event.favorite)

            is DetailUiEvent.OnTitleChange -> updateTitle(event.title)

            is DetailUiEvent.OnAuthorChange -> updateAuthor(event.author)

            is DetailUiEvent.OnNavigationToRead -> navigate("${Routes.READ}/${bookUuid}/${Routes.BOOK_DETAIL}/${event.curPage}")


            is DetailUiEvent.OnDeleteReadRecord -> deleteReadRecord()

            is DetailUiEvent.OnShareBookFile -> shareBook()

            is DetailUiEvent.OnCopyContent -> copyContent()
        }
    }

    private fun updateFavorite(isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = FavoriteBook(bookUuid)
            if (isFavorite) {
                DatabaseHelper.favoriteBookDao().insert(data)
            } else {
                DatabaseHelper.favoriteBookDao().delete(data)
            }
        }
    }

    private fun updateTitle(title: String) {
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        book.title = title
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHelper.bookDao().update(book)
        }
    }

    private fun updateAuthor(author: String) {
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        book.author = author
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHelper.bookDao().update(book)
        }
    }

    private fun deleteReadRecord() {
        val readRecord = _uiState.value.readRecord ?: return
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHelper.readRecordDao().delete(readRecord)
        }
    }

    private fun shareBook() {
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        AppUtil.shareFile(book.uri, book.mimeType)
    }

    private fun copyContent() {
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        val readRecord = _uiState.value.readRecord
        val json = buildJsonObject {
            put(globalContext.getString(R.string.source), book.uri.storagePath())
            put(globalContext.getString(R.string.author), book.author)
            put(
                globalContext.getString(R.string.read_progress),
                if (readRecord == null) "None" else "${readRecord.curPage}/${readRecord.pageCount}"
            )
        }.toString()
        val content = JSONObject(json).toString(2).replace("\\", "")
        ClipboardUtil.writeTo(content)
    }
}