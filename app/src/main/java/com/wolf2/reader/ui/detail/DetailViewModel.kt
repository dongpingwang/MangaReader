package com.wolf2.reader.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.FavoriteBook
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.ExtraInfo
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.LocalFileReader
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.util.LoadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class DetailUiEvent {
    data object OnBackHandle : DetailUiEvent()
    data class OnFavoriteChange(val favorite: Boolean) : DetailUiEvent()
    data class OnTitleChange(val title: String) : DetailUiEvent()
    data class OnAuthorChange(val author: String) : DetailUiEvent()
    data object OnNavigationToRead : DetailUiEvent()
    data object DeleteReadRecord : DetailUiEvent()
}

data class DetailUiState(
    val bookResult: LoadResult<Book> = LoadResult.Loading,
    val favorite: Boolean = false,
    val readRecord: ReadRecord? = null,
    val snackbar: Boolean? = null
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

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                val book = DatabaseHelper.bookDao().queryByUuid(bookUuid)
                if (book == null) {
                    Timber.e("book from db is null")
                    _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
                    return@launch
                }
                val readRecord = DatabaseHelper.readRecordDao().getReadRecord(bookUuid)
                // TODO 看要不要放在添加书籍时来解析
                if (book.extraInfo.pageCount <= 0) {
                    LocalFileReader(book).apply {
                        readBook(updateMetadata = false)
                        close()
                    }
                    book.extraInfo = ExtraInfo(
                        pageCount = book.pageContents.size,
                        chapterCount = book.chapters.size
                    )
                    DatabaseHelper.bookDao().update(book)
                }

                val favorite = DatabaseHelper.favoriteBookDao().getFavoriteBook(bookUuid) != null
                _uiState.update {
                    it.copy(
                        bookResult = LoadResult.Success(book),
                        readRecord = readRecord,
                        favorite = favorite
                    )
                }
            }
        }
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
                val book = (uiState.value.bookResult as LoadResult.Success<Book>).data
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

            is DetailUiEvent.DeleteReadRecord -> {
                val readRecord = _uiState.value.readRecord ?: return
                viewModelScope.launch(Dispatchers.IO) {
                    DatabaseHelper.readRecordDao().delete(readRecord)
                }
            }
        }
    }
}