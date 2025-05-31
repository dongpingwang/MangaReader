package com.wolf2.reader.ui.read


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linxiao.framework.common.globalContext
import com.wolf2.reader.R
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.ImageQuality
import com.wolf2.reader.config.ImageScale
import com.wolf2.reader.config.PageSwitchEffect
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.reader.LocalFileReader
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.LoadResult
import com.wolf2.reader.util.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class ReadUiState(
    val imageQuality: ImageQuality = AppConfig.imageQualityLD.value!!,
    val imageScale: ImageScale = AppConfig.imageScaleLD.value!!,
    val pagerSwitchEffect: PageSwitchEffect = AppConfig.pagerSwitchEffectLD.value!!,
    val darkMode: Boolean = false,
    val readRecord: ReadRecord = ReadRecord(),
    val bookResult: LoadResult<Book> = LoadResult.Loading,
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
    private var readRecord: ReadRecord? = null

    private val imageQualityObserver = object : (ImageQuality) -> Unit {
        override fun invoke(v: ImageQuality) {
            _uiState.update { it.copy(imageQuality = v) }
        }
    }
    private val imageScaleObserver = object : (ImageScale) -> Unit {
        override fun invoke(v: ImageScale) {
            _uiState.update { it.copy(imageScale = v) }
        }
    }
    private val pagerSwitchEffectObserver = object : (PageSwitchEffect) -> Unit {
        override fun invoke(v: PageSwitchEffect) {
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
                    it.imageQualityLD.observeForever(imageQualityObserver)
                    it.imageScaleLD.observeForever(imageScaleObserver)
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
                var record = DatabaseHelper.readRecordDao().getReadRecord(bookId) ?: ReadRecord(
                    bookUuid = book.uuid,
                    pageCount = book.pageContents.size,
                    lastReadTimeMillis = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        bookResult = LoadResult.Success(book),
                        readRecord = record
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
        readRecord?.let { r -> _uiState.update { it.copy(readRecord = r) } }
    }

    fun getCurPage(): Int {
        readRecord?.let { return it.curPage }
        return _uiState.value.curPage
    }

    fun toggleDarkMode() {
        updateRecordOnMemory()
        AppConfig.darkModeLD.postValue(AppConfig.darkModeLD.value != true)
    }

    fun setPagerSwitchEffect(effect: PageSwitchEffect) {
        updateRecordOnMemory()
        AppConfig.pagerSwitchEffectLD.postValue(effect)
    }

    fun setImageQuality(quality: ImageQuality) {
        updateRecordOnMemory()
        AppConfig.imageQualityLD.postValue(quality)
    }

    fun setImageScale(scale: ImageScale) {
        updateRecordOnMemory()
        AppConfig.imageScaleLD.postValue(scale)
    }

    fun cacheImage() {
        viewModelScope.launch(Dispatchers.IO) {
            val book = (_uiState.value.bookResult as LoadResult.Success<Book>).data
            val pageContent = book.pageContents[getCurPage()]
            val buffer = fileReader?.getImageBuffer(pageContent)
            val ret = if (buffer == null) false else ImageCacheUtil.cacheImage(buffer)
            Timber.d("cacheImage: ret = $ret")
            if (!ret) return@launch
            ToastUtil.toastOnUiThread(
                globalContext.getString(
                    R.string.image_cache_success,
                    ImageCacheUtil.cacheImageDir
                )
            )
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
                    it.imageQualityLD.removeObserver(imageQualityObserver)
                    it.imageScaleLD.removeObserver(imageScaleObserver)
                    it.pagerSwitchEffectLD.removeObserver(pagerSwitchEffectObserver)
                    it.darkModeLD.removeObserver(darkModeObserver)
                }
            }
        }
    }
}