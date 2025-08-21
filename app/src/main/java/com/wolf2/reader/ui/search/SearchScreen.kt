package com.wolf2.reader.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wolf2.reader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.provideFactory())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var keyword by rememberSaveable { mutableStateOf("") }
    val searchResult = uiState.searchResult
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { traversalIndex = 0f },
                inputField = {
                    SearchBarDefaults.InputField(
                        query = keyword,
                        onQueryChange = {
                            keyword = it
                            if (it.isNotEmpty()) {
                                viewModel.onEvent(SearchUiEvent.OnSearch(it))
                            }
                        },
                        onSearch = {
                            if (it.isNotEmpty()) {
                                viewModel.onEvent(SearchUiEvent.OnSearch(it))
                            }
                        },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                keyword.isNotEmpty()
                            ) {
                                IconButton(onClick = {
                                    keyword = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { },
            ) {

            }

        }, navigationIcon = {
            IconButton(onClick = {
                viewModel.onEvent(SearchUiEvent.OnBackHandle)
            }) {
                Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
            }
        })

        if (keyword.isEmpty()) {
            // 默认状态
        }
        if (keyword.isNotEmpty() && searchResult.isEmpty()) {
            // 没有搜索结果
        }

        if (keyword.isNotEmpty() && searchResult.isNotEmpty()) {
            // 搜索到结果
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(100.dp),
                verticalItemSpacing = 4.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    items(searchResult) {
                        Card(onClick = { viewModel.onEvent(SearchUiEvent.OnNavigationToDetail(it.uuid)) }) {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(
                                text = it.author,
                                style = MaterialTheme.typography.bodySmall,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}