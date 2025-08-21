package com.wolf2.reader.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.ui.home.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(val searchResult: List<Book> = emptyList())

sealed class SearchUiEvent {
    data object OnBackHandle : SearchUiEvent()
    data class OnSearch(val keyword: String) : SearchUiEvent()
    data class OnNavigationToDetail(val bookUuid: String) : SearchUiEvent()
}

class SearchViewModel : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel() as T
                }
            }
    }

    private var _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.OnBackHandle -> popBackStack()
            is SearchUiEvent.OnSearch -> search(event.keyword)
            is SearchUiEvent.OnNavigationToDetail -> navigate("${Routes.BOOK_DETAIL}/${event.bookUuid}")
        }
    }

    private fun search(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = DatabaseHelper.bookDao().search(keyword)
            _uiState.update { it.copy(books) }
        }
    }
}