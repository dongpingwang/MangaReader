package com.wolf2.reader.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastJoinToString
import com.anggrayudi.storage.extension.isTreeDocumentFile
import com.wolf2.reader.R
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.EbookUtil
import com.wolf2.reader.popBackStack
import com.wolf2.reader.util.getPersistedUriPermissions
import com.wolf2.reader.util.releasePersistableUriPermission
import com.wolf2.reader.util.storagePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.MultiSelectListPreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafScreen() {
    val scope = rememberCoroutineScope()

    fun persistedUris(): List<String> {
        return getPersistedUriPermissions().filter { it.uri.isTreeDocumentFile }.map { it.uri.storagePath().toString() }
    }

    var uris by remember { mutableStateOf(persistedUris()) }
    fun releaseUriPermission(keeps: Set<String>) {
        scope.launch(Dispatchers.IO) {
            val saved = getPersistedUriPermissions()
            saved.fastForEach {
                if (!keeps.contains(it.uri.storagePath().toString())) {
                    it.uri.releasePersistableUriPermission()
                }
            }
            uris = persistedUris()
        }
    }

    fun getFileFormats(): Set<String> = AppConfig.fileFormats
    var fileFormats by remember { mutableStateOf(getFileFormats().toList()) }
    fun updateFileFormats(keep: Set<String>) {
        AppConfig.fileFormats = keep
        fileFormats = getFileFormats().toList()
    }

    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.settings_saf))
        }, navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack, contentDescription = null
                )
            }
        })

        ProvidePreferenceLocals {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    MultiSelectListPreference(
                        value = uris.toSet(),
                        onValueChange = {
                            releaseUriPermission(it)
                        },
                        values = uris,
                        title = {
                            Text(text = stringResource(R.string.settings_book_folder))
                        }, summary = {
                            Text(text = uris.fastJoinToString(), overflow = TextOverflow.Ellipsis)
                        })
                }

                item {
                    MultiSelectListPreference(
                        value = fileFormats.toSet(),
                        onValueChange = {
                            updateFileFormats(it)
                        },
                        values = EbookUtil.allFileFormats,
                        title = {
                            Text(text = stringResource(R.string.settings_book_format))
                        }, summary = {
                            Text(
                                text = fileFormats.fastJoinToString(),
                                overflow = TextOverflow.Ellipsis
                            )
                        })
                }
            }
        }
    }
}