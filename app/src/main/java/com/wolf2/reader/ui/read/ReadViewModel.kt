package com.wolf2.reader.ui.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.reader.LocalFileReader
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.GalleryUtil
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.MD5Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class ReadUiEvent {
    data object OnSnackbarDismiss : ReadUiEvent()
    data object OnDisplayCacheImage : ReadUiEvent()
}

data class ReadUiState(
    val pagerSwitchEffect: Int = AppConfig.pagerSwitchEffectLD.value!!,
    val darkMode: Boolean = false,
    val readRecord: ReadRecord = ReadRecord(),
    val bookResult: LoadResult<Book> = LoadResult.Loading,
    var snackbar: Boolean? = null
) {
    val curPage: Int
        get() = readRecord.curPage
}

class ReadViewModel(val bookId: String) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(bookId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReadViewModel(bookId) as T
                }
            }
    }

    private var _uiState = MutableStateFlow(ReadUiState())
    val uiState = _uiState.asStateFlow()

    private var fileReader: LocalFileReader? = null
    private lateinit var readRecord: ReadRecord
    private var cacheImagePath: String? = null

    private val pagerSwitchEffectObserver = object : (Int) -> Unit {
        override fun invoke(v: Int) {
            _uiState.update { it.copy(pagerSwitchEffect = v) }
        }
    }

    private val darkModeObserver = object : (Boolean) -> Unit {
        override fun invoke(v: Boolean) {
            _uiState.update { it.copy(darkMode = v) }
        }
    }

    init {
        viewModelScope.launch {
            launch(Dispatchers.Main) {
                AppConfig.let {
                    it.pagerSwitchEffectLD.observeForever(pagerSwitchEffectObserver)
                    it.darkModeLD.observeForever(darkModeObserver)
                }
            }

            launch(Dispatchers.IO) {
                val book = DatabaseHelper.bookDao().queryByUuid(bookId)
                if (book == null) {
                    Timber.e("book from db is null")
                    _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
                    return@launch
                }
                val reader = LocalFileReader(book)
                fileReader = reader
                if (!reader.readBook()) {
                    Timber.e("readBook fail")
                    _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
                    return@launch
                }
                readRecord = DatabaseHelper.readRecordDao().getReadRecord(bookId) ?: ReadRecord(
                    bookUuid = book.uuid,
                    pageCount = book.pageContents.size,
                    lastReadTimeMillis = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        bookResult = LoadResult.Success(book),
                        readRecord = readRecord
                    )
                }
            }
        }
    }

    fun updateReadRecord(curPage: Int, updateImmediately: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRecord = _uiState.value.readRecord.copy(
                curPage = curPage,
                lastReadTimeMillis = System.currentTimeMillis()
            )
            DatabaseHelper.readRecordDao().insert(newRecord)
            // 不更新UI，防止闪烁
            readRecord = newRecord
            if (updateImmediately) {
                _uiState.update { it.copy(readRecord = newRecord) }
            }
        }
    }

    fun updateRecordOnMemory() {
        _uiState.update { it.copy(readRecord = readRecord) }
    }

    fun getCurPage(): Int {
        return readRecord.curPage
    }

    fun toggleDarkMode() {
        updateRecordOnMemory()
        AppConfig.darkModeLD.postValue(AppConfig.darkModeLD.value != true)
    }

    fun setPagerSwitchEffect(effect: Int) {
        updateRecordOnMemory()
        AppConfig.pagerSwitchEffectLD.postValue(effect)
    }

    fun cacheImage() {
        viewModelScope.launch(Dispatchers.IO) {
            val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
            val pageContent = book.pageContents[getCurPage()]
            val buffer = fileReader?.getImageBuffer(pageContent)
            val displayName =
                MD5Util.getMD5String16(pageContent.pageHref + pageContent.markupUid, null)
            val ret = if (buffer == null) false else ImageCacheUtil.cacheImage(buffer, displayName)
            Timber.d("cacheImage: ret = $ret")
            if (!ret) return@launch
            cacheImagePath = ImageCacheUtil.getCacheImageDiskPath(displayName)
            updateRecordOnMemory()
            _uiState.update { it.copy(snackbar = true) }
        }
    }

    fun getImageBuffer(page: PageContent): ByteArray? {
        return fileReader?.getImageBuffer(page)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            launch(Dispatchers.Main) {
                AppConfig.let {
                    it.pagerSwitchEffectLD.removeObserver(pagerSwitchEffectObserver)
                    it.darkModeLD.removeObserver(darkModeObserver)
                }
            }
        }
    }

    fun onEvent(event: ReadUiEvent) {
        when (event) {
            is ReadUiEvent.OnSnackbarDismiss -> {
                _uiState.update { it.copy(snackbar = null) }
            }

            is ReadUiEvent.OnDisplayCacheImage -> {
                updateRecordOnMemory()
                _uiState.update { it.copy(snackbar = null) }
                cacheImagePath?.let { GalleryUtil.openImageInGallery(it) }
            }
        }
    }
}