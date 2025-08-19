package com.wolf2.reader.ui.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.R
import com.wolf2.reader.util.MD5Util
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.mmkvEmit
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.BookMark
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.reader.CachedReader
import com.wolf2.reader.reader.numbers
import com.wolf2.reader.reader.percent
import com.wolf2.reader.ui.common.SnackbarModel
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.globalContext
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
    data object OnBookMarkToggle : ReadUiEvent()
    data object OnCacheImage : ReadUiEvent()
    data class OnPageSwitchEffectChange(val effect: Int) : ReadUiEvent()
    data object OnPrevChapter : ReadUiEvent()
    data object OnNextChapter : ReadUiEvent()
    data class OnPageChange(val pageInt: Int, val updateImmediately: Boolean) : ReadUiEvent()

}

data class ReadUiState(
    val pagerSwitchEffect: Int = AppConfig.pagerSwitchEffect.value,
    val darkMode: Boolean = AppConfig.darkMode.value,
    val readRecord: ReadRecord = ReadRecord(),
    val bookMarks: MutableList<BookMark> = mutableListOf(),
    val bookResult: LoadResult<Book> = LoadResult.Loading,
    val snackbar: SnackbarModel? = null
) {
    val curPage: Int
        get() = readRecord.curPage
}

class ReadViewModel(val bookUuid: String, val from: String) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(bookUuid: String, from: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReadViewModel(bookUuid, from) as T
                }
            }
    }

    private var _uiState = MutableStateFlow(ReadUiState())
    val uiState = _uiState.asStateFlow()

    private val _curPageFlow = MutableStateFlow(0)
    val curPageFlow = _curPageFlow.asStateFlow()

    private var fileReader: CachedReader? = null
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
                val reader = CachedReader.obtainLocalFileReader(book)
                fileReader = reader
                if (!reader.copyOrRead(to = book, updateMetadata = false)) {
                    Timber.e("readBook fail")
                    _uiState.update { it.copy(bookResult = LoadResult.Error(Throwable())) }
                    return@launch
                }
                readRecord = DatabaseHelper.readRecordDao().getReadRecord(bookUuid)
                    ?.also { r ->
                        existsReadRecord = true
                        _curPageFlow.update { r.curPage }
                    }
                    ?: ReadRecord(
                        bookUuid = book.uuid,
                        pageCount = book.extraInfo.pageCount,
                        lastReadTimeMillis = System.currentTimeMillis()
                    )
                val bookMarks =
                    DatabaseHelper.bookMarkDao().queryByBookUuid(bookUuid).toMutableList()
                _uiState.update {
                    it.copy(
                        bookResult = LoadResult.Success(book),
                        bookMarks = bookMarks,
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

    override fun onCleared() {
        super.onCleared()
        if (from != Routes.BOOK_DETAIL) {
            fileReader?.close()
        }
    }

    fun updateReadRecord(curPage: Int, updateImmediately: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRecord = _uiState.value.readRecord.copy(
                curPage = curPage,
                lastReadTimeMillis = System.currentTimeMillis()
            )
            // 减少更新UI，防止闪烁
            readRecord = newRecord
            _curPageFlow.update { curPage }
            if (updateImmediately) {
                _uiState.update { it.copy(readRecord = newRecord) }
            }
            if (existsReadRecord) {
                DatabaseHelper.readRecordDao().update(newRecord)
            } else {
                existsReadRecord = true
                DatabaseHelper.readRecordDao().insert(newRecord)
            }
        }
    }


    fun getCurPage(): Int {
        return readRecord.curPage
    }

    private fun getCurChapterTriple(): Triple<Int, Chapter, List<Chapter>>? {
        val chapters = (_uiState.value.bookResult as LoadResult.Success<Book>).data.chapters
        if (chapters.size <= 1) return null
        val curPage = getCurPage()
        var chapterIndex = 0
        for ((i, c) in chapters.withIndex()) {
            if (c.pageIndexRange.contains(curPage)) {
                chapterIndex = i
                break
            }
        }
        return Triple(chapterIndex, chapters[chapterIndex], chapters)
    }

    fun getCurChapterPageRange(): IntRange {
        val triple = getCurChapterTriple()
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        if (triple == null) {
            return IntRange(0, book.pageContents.size - 1)
        }
        return triple.second.pageIndexRange
    }

    fun getChapterTitle(): String {
        val triple = getCurChapterTriple()
        val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
        if (triple == null) {
            return book.title
        }
        return triple.second.title
    }

    fun getChapterProgress(): String {
        val triple = getCurChapterTriple()
        if (triple == null) {
            val percent = readRecord.percent()
            val numbers = readRecord.numbers()
            return "$numbers $percent"
        }
        val range = triple.second.pageIndexRange
        val progress = getCurPage()
        val percent = range.percent(progress)
        val numbers = range.numbers(progress)
        return "$numbers $percent"
    }

    fun isCurPageBookMarked(): Boolean {
        val marked = _uiState.value.bookMarks.find { getCurPage() == it.pageIndex } != null
        return marked
    }

    private fun setPagerSwitchEffect(effect: Int) {
        _uiState.update { it.copy(readRecord = readRecord) }
        AppConfig.pagerSwitchEffect.mmkvEmit(effect)
    }

    private fun cacheImage() {
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
            _uiState.update { it.copy(snackbar = cacheImageSnackbar, readRecord = readRecord) }
        }
    }

    fun getImageBuffer(page: PageContent): ByteArray? {
        return fileReader?.getImageBuffer(page)
    }

    private fun toggleBookMark() {
        viewModelScope.launch(Dispatchers.IO) {
            val bookMarks = _uiState.value.bookMarks
            val findMark = bookMarks.find { it.pageIndex == getCurPage() }
            if (findMark != null) {
                bookMarks.remove(findMark)
                DatabaseHelper.bookMarkDao().delete(findMark)
            } else {
                val mark = BookMark(bookUuid = bookUuid, pageIndex = getCurPage())
                bookMarks.add(mark)
                DatabaseHelper.bookMarkDao().insert(mark)
            }
            _uiState.update { it.copy(bookMarks = bookMarks, readRecord = readRecord) }
        }
    }

    private fun prevChapter() {
        val triple = getCurChapterTriple()
        val chapterIndex = triple?.first ?: return
        if (chapterIndex <= 0) {
            Timber.d("no previous chapter")
            return
        }
        val pageIndex = triple.third[chapterIndex - 1].pageIndexRange.first
        updateReadRecord(pageIndex, true)
    }

    private fun nextChapter() {
        val triple = getCurChapterTriple()
        val chapterIndex = triple?.first ?: return
        val chapters = triple.third
        if (chapterIndex >= chapters.size - 1) {
            Timber.d("no next chapter")
            return
        }
        val pageIndex = chapters[chapterIndex + 1].pageIndexRange.first
        updateReadRecord(pageIndex, true)
    }

    fun onEvent(event: ReadUiEvent) {
        when (event) {
            is ReadUiEvent.OnSnackbarDismiss -> _uiState.update { it.copy(snackbar = null) }

            is ReadUiEvent.OnDisplayCacheImage -> cacheImgName?.let { navigate("${Routes.IMAGE_PREVIEW}/$it") }

            is ReadUiEvent.OnBackHandle -> popBackStack()

            is ReadUiEvent.OnBookMarkToggle -> toggleBookMark()

            is ReadUiEvent.OnCacheImage -> cacheImage()

            is ReadUiEvent.OnPageSwitchEffectChange -> setPagerSwitchEffect(event.effect)

            is ReadUiEvent.OnPrevChapter -> prevChapter()

            is ReadUiEvent.OnNextChapter -> nextChapter()

            is ReadUiEvent.OnPageChange -> updateReadRecord(event.pageInt, event.updateImmediately)
        }
    }

    private val cacheImageSnackbar =
        SnackbarModel(
            message = globalContext.getString(R.string.image_cache_success),
            actionLabel = globalContext.getString(R.string.display_image_cache),
            withDismissAction = true,
            actionPerformed = {
                onEvent(ReadUiEvent.OnSnackbarDismiss)
                onEvent(ReadUiEvent.OnDisplayCacheImage)
            },
            dismissed = { onEvent(ReadUiEvent.OnSnackbarDismiss) }
        )
}