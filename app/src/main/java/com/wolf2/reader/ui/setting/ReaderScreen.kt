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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wolf2.reader.R
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.mmkvEmit
import com.wolf2.reader.popBackStack
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen() {
    val pagerEffectFlow = AppConfig.pagerSwitchEffect.collectAsStateWithLifecycle()
    val pageEffects =
        LocalContext.current.resources.getStringArray(R.array.read_page_effects).toList()
    val curPagerEffect = pageEffects[pagerEffectFlow.value]

    val readerFullScreenFlow = AppConfig.readerFullScreen.collectAsStateWithLifecycle()

    val darkModeFlow = AppConfig.darkMode.collectAsStateWithLifecycle()

    val chapterDisplayFlow = AppConfig.chapterDisplay.collectAsStateWithLifecycle()
    val chapterDisplays =
        LocalContext.current.resources.getStringArray(R.array.chapter_display).toList()
    val curChapterDisplay = chapterDisplays[chapterDisplayFlow.value]

    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.settings_reader))
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
                    ListPreference(
                        value = curPagerEffect,
                        onValueChange = {
                            AppConfig.pagerSwitchEffect.mmkvEmit(pageEffects.indexOf(it))
                        },
                        values = pageEffects,
                        title = {
                            Text(text = stringResource(R.string.settings_reader_mode))
                        },
                        summary = {
                            Text(text = curPagerEffect)
                        },
                        type = ListPreferenceType.DROPDOWN_MENU
                    )
                }

                item {
                    ListPreference(
                        value = curChapterDisplay,
                        onValueChange = {
                            AppConfig.chapterDisplay.mmkvEmit(chapterDisplays.indexOf(it))
                        },
                        values = chapterDisplays,
                        title = {
                            Text(text = stringResource(R.string.settings_reader_chapter_display))
                        },
                        summary = {
                            Text(text = curChapterDisplay)
                        },
                        type = ListPreferenceType.DROPDOWN_MENU
                    )
                }

                item {
                    SwitchPreference(
                        value = readerFullScreenFlow.value,
                        onValueChange = {
                            AppConfig.readerFullScreen.mmkvEmit(it)
                        },
                        title = {
                            Text(text = stringResource(R.string.settings_reader_full_screen))
                        },
                        summary = {
                            Text(text = stringResource(R.string.settings_reader_full_screen_summary))
                        }
                    )
                }

                item {
                    SwitchPreference(
                        value = darkModeFlow.value,
                        onValueChange = {
                            AppConfig.darkMode.mmkvEmit(it)
                        },
                        title = {
                            Text(text = stringResource(R.string.settings_reader_black_background))
                        },
                        summary = {
                            Text(text = stringResource(R.string.settings_reader_black_background_summary))
                        }
                    )
                }
            }
        }
    }
}