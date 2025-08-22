package com.wolf2.reader.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.ShelfFilter
import com.wolf2.reader.config.ShelfSort
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.navigate
import com.wolf2.reader.ui.home.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class BookShelfUiEvent {
    data object OnNavigationToBrowser : BookShelfUiEvent()
    data class OnItemClick(val book: Book) : BookShelfUiEvent()
    data class OnItemLongClick(val book: Book) : BookShelfUiEvent()
}

data class BookShelfUiState(
    val readRecords: List<ReadRecord> = emptyList(),
    val curFilter: Int = AppConfig.shelfFilter.value,
    val curSort: Int = AppConfig.shelfSort.value
)

class BookShelfViewModel : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BookShelfViewModel() as T
                }
            }
    }

    private val _uiState = MutableStateFlow(BookShelfUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                DatabaseHelper.readRecordDao().observeAll().collectLatest {
                    val readRecords = it
                    _uiState.update {
                        it.copy(readRecords = readRecords)
                    }
                }
            }

            launch {
                AppConfig.shelfFilter.collectLatest { v ->
                    _uiState.update { it.copy(curFilter = v) }
                }
            }

            launch {
                AppConfig.shelfSort.collectLatest { v ->
                    _uiState.update { it.copy(curSort = v) }
                }
            }
        }
    }

    private fun pager(source: () -> PagingSource<Int, Book>): Pager<Int, Book> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = true,
                maxSize = 30
            ),
            pagingSourceFactory = source
        )
    }

    private val allBooksSortReadTime = pager { DatabaseHelper.bookDao().allBooksSortReadTime() }

    private val allBooksSortTitle = pager { DatabaseHelper.bookDao().allBooksSortTitle() }

    private val allBooksSortAuthor = pager { DatabaseHelper.bookDao().allBooksSortAuthor() }

    private val allBooksFilterReading = pager { DatabaseHelper.bookDao().allBooksFilterReading() }

    private val allBooksFilterUnRead = pager { DatabaseHelper.bookDao().allBooksFilterUnRead() }

    private val allBooksFilterFavorite = pager { DatabaseHelper.bookDao().allBooksFilterFavorite() }

    fun getPager(sort: Int, filter: Int): Pager<Int, Book> {
        var pager = when (ShelfSort.fromInt(sort)) {
            ShelfSort.LastReadTime -> allBooksSortReadTime
            ShelfSort.Title -> allBooksSortTitle
            ShelfSort.Author -> allBooksSortAuthor
        }
        pager = when (ShelfFilter.fromInt(filter)) {
            ShelfFilter.ALL -> pager
            ShelfFilter.Reading -> allBooksFilterReading
            ShelfFilter.UnRead -> allBooksFilterUnRead
            ShelfFilter.Favorite -> allBooksFilterFavorite
        }
        return pager
    }

    fun onEvent(event: BookShelfUiEvent) {
        when (event) {
            is BookShelfUiEvent.OnNavigationToBrowser -> {
                navigate(Routes.BROWSER_BOOK)
            }

            is BookShelfUiEvent.OnItemClick -> {
                navigate("${Routes.BOOK_DETAIL}/${event.book.uuid}")
            }

            is BookShelfUiEvent.OnItemLongClick -> {

            }
        }
    }
}