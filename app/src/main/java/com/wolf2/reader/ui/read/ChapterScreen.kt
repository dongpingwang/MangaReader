package com.wolf2.reader.ui.read

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.shelf.BookShelfViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(bookUuid: String) {
    val shelfViewModel: BookShelfViewModel = viewModel(
        factory = BookShelfViewModel.provideFactory(),
        viewModelStoreOwner = globalViewContext?.owner ?: LocalViewModelStoreOwner.current!!
    )
    val shelfUiState by shelfViewModel.uiState.collectAsStateWithLifecycle()
    val book = shelfUiState.books.toMutableList().firstOrNull { it.uuid == bookUuid } ?: return
    val readViewModel: ReadViewModel = viewModel(factory = ReadViewModel.provideFactory(bookUuid))
    readViewModel.injectBook(book)
    val readUiState by readViewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(navigationIcon = {
            IconButton(onClick = {
                globalViewContext?.navController?.popBackStack()
            }) {
                Icon(imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = null)
            }
        }, title = {
            var selectedIndex by remember { mutableIntStateOf(0) }
            val options = listOf("目录", "书签", "笔记")

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = { selectedIndex = index },
                        selected = index == selectedIndex,
                        label = { Text(label) }
                    )
                }
            }
        }, actions = {
            TextButton(onClick = {}) {
                Text("正序")
            }
        })

        LazyColumn {
            items(readUiState.book.chapters) {
                NavigationDrawerItem(
                    label = { Text(text = it.title) },
                    selected = false,
                    onClick = {}
                )
            }
        }

    }
}