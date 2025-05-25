package com.wolf2.reader.ui.read

import android.os.Environment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.linxiao.framework.common.globalContext
import com.wolf2.reader.config.PageSwitchEffect
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.shelf.BookShelfViewModel
import com.wolf2.reader.ui.common.LoadingIndicator
import com.wolf2.reader.ui.read.component.CurlPageContent
import com.wolf2.reader.ui.read.component.ErrorIndicator
import com.wolf2.reader.ui.read.component.ReadBottomContent
import com.wolf2.reader.ui.read.component.ReadTopAppBar
import com.wolf2.reader.ui.read.component.VHPagerContent
import timber.log.Timber

@Composable
fun ReadScreen(bookUuid: String) {
    val shelfViewModel: BookShelfViewModel = viewModel(
        factory = BookShelfViewModel.provideFactory(),
        viewModelStoreOwner = globalViewContext?.owner ?: LocalViewModelStoreOwner.current!!
    )
    val shelfUiState by shelfViewModel.uiState.collectAsStateWithLifecycle()
    val book = shelfUiState.books.toMutableList().firstOrNull { it.uuid == bookUuid }
    if (book == null) {
        Timber.d("book is null, please notice")
        ErrorIndicator()
        return
    }
    DocumentFile.fromSingleUri(LocalContext.current, book.uri).let {
        if (it?.exists() == false || it?.canRead() == false) {
            ErrorIndicator()
            Timber.d("book file isn't exists, please notice")
            return
        }
    }

    if (!Environment.isExternalStorageManager()){
        ErrorIndicator()
        Timber.d("Access File Not")
        return
    }

    val isNeedParse = book.let {
        shelfViewModel.needParseFile(it)
    }
    val readViewModel: ReadViewModel = viewModel(factory = ReadViewModel.provideFactory(bookUuid))
    val readUiState by readViewModel.uiState.collectAsStateWithLifecycle()
    var showBar by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        var parsing = shelfUiState.bookParseStatus.isLoading()
        if (isNeedParse || parsing) {
            LoadingIndicator()
        } else {
            readViewModel.injectBook(book)
            when (readUiState.pagerSwitchEffect) {
                PageSwitchEffect.VerticalPage -> VHPagerContent(
                    readViewModel = readViewModel,
                    isVerticalPager = true,
                    readUiState = readUiState,
                    onImageClick = { showBar = !showBar }
                )

                PageSwitchEffect.HorizontalPage -> VHPagerContent(
                    readViewModel = readViewModel,
                    isVerticalPager = false,
                    readUiState = readUiState,
                    onImageClick = { showBar = !showBar }
                )

                PageSwitchEffect.CurlPage -> CurlPageContent(
                    readViewModel = readViewModel,
                    readUiState = readUiState,
                    onImageClick = { showBar = !showBar }
                )
            }
        }

        if (showBar) {
            ReadTopAppBar(readViewModel)
            ReadBottomContent(readViewModel, readUiState)
        }
    }
}



