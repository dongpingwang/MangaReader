package com.wolf2.reader.ui.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.util.MD5Util
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.mmkvEmit
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.LocalFileReader
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.LoadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class ReadUiEvent {
    data object OnSnackbarDismiss : ReadUiEvent()
    data object OnDisplayCacheImage : ReadUiEvent()
    data object OnBackHandle : ReadUiEvent()
    data object OnDestroy : ReadUiEvent()
}

data class ReadUiState(
    val pagerSwitchEffect: Int = AppConfig.pagerSwitchEffect.value,
    val darkMode: Boolean = AppConfig.darkMode.value,
    val readRecord: ReadRecord = ReadRecord(),
    val bookResult: LoadResult<Book> = LoadResult.Loading,
    val snackbar: Boolean? = null
) {
    val curPage: Int
        get() = readRecord.curPage
}

class ReadViewModel(val bookUuid: String) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(bookUuid: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReadViewModel(bookUuid) as T
                }
            }
    }

    private var _uiState = MutableStateFlow(ReadUiState())
    val uiState = _uiState.asStateFlow()

    private var fileReader: LocalFileReader? = null
    private var existsReadRecord = false
    private lateinit var readRecord: ReadRecord
    private var cacheImgName: String? = null

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                val book = DatabaseHelper.bookDao().queryByUuid(bookUuid)
                if (book == null) {
                    Timber.e("book from db is null")
                    _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
                    return@launch
                }
                val reader = LocalFileReader.withLocalFileReader(book)
                fileReader = reader
                if (!reader.readBook(updateMetadata = false)) {
                    Timber.e("readBook fail")
                    _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
                    return@launch
                }
                readRecord = DatabaseHelper.readRecordDao().getReadRecord(bookUuid)
                    ?.also {
                        existsReadRecord = true
                    }
                    ?: ReadRecord(
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

            launch {
                AppConfig.pagerSwitchEffect.collectLatest { v ->
                    _uiState.update { it.copy(pagerSwitchEffect = v) }
                }
            }

            launch {
                AppConfig.darkMode.collectLatest { v ->
                    _uiState.update { it.copy(darkMode = v) }
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
            if (existsReadRecord) {
                DatabaseHelper.readRecordDao().update(newRecord)
            } else {
                existsReadRecord = true
                DatabaseHelper.readRecordDao().insert(newRecord)
            }
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
        AppConfig.darkMode.mmkvEmit(!AppConfig.darkMode.value)
    }

    fun setPagerSwitchEffect(effect: Int) {
        updateRecordOnMemory()
        AppConfig.pagerSwitchEffect.mmkvEmit(effect)
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
            cacheImgName = displayName
            updateRecordOnMemory()
            _uiState.update { it.copy(snackbar = true) }
        }
    }

    fun getImageBuffer(page: PageContent): ByteArray? {
        return fileReader?.getImageBuffer(page)
    }

    fun onEvent(event: ReadUiEvent) {
        when (event) {
            is ReadUiEvent.OnSnackbarDismiss -> {
                _uiState.update { it.copy(snackbar = null) }
            }

            is ReadUiEvent.OnDisplayCacheImage -> {
                updateRecordOnMemory()
                _uiState.update { it.copy(snackbar = null) }
                cacheImgName?.let { navigate("${Routes.IMAGE_PREVIEW}/$it") }
            }

            is ReadUiEvent.OnBackHandle -> popBackStack()

            is ReadUiEvent.OnDestroy -> fileReader?.close()
        }
    }
}