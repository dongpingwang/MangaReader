package com.wolf2.reader.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wolf2.reader.R
import com.wolf2.reader.navigate
import com.wolf2.reader.popBackStack
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.util.AppUtil
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.menu_settings))
        }, navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack, contentDescription = null
                )
            }
        })

        ProvidePreferenceLocals {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                preference(key = "appearance", title = {
                    Text(text = stringResource(R.string.settings_appearance))
                }, icon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null
                    )
                }, summary = {
                    Text(text = stringResource(R.string.settings_appearance_summary))
                }, onClick = {
                    navigate(Routes.SETTINGS_APPEARANCE)
                })

                preference(key = "reader", title = {
                    Text(text = stringResource(R.string.settings_reader))
                }, icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocalLibrary,
                        contentDescription = null
                    )
                }, summary = {
                    Text(text = stringResource(R.string.settings_reader_summary))
                }, onClick = {
                    navigate(Routes.SETTINGS_READER)
                })

                preference(key = "download", title = {
                    Text(text = stringResource(R.string.settings_download))
                }, icon = {
                    Icon(imageVector = Icons.Outlined.Download, contentDescription = null)
                }, summary = {
                    Text(text = stringResource(R.string.settings_download_summary))
                }, onClick = {
                    navigate(Routes.SETTINGS_DOWNLOAD)
                })

                preference(key = "develop", title = {
                    Text(text = stringResource(R.string.settings_develop))
                }, icon = {
                    Icon(imageVector = Icons.Outlined.DeveloperMode, contentDescription = null)
                }, summary = {
                    Text(text = stringResource(R.string.settings_develop_summary))
                }, onClick = {})

                preference(key = "about", title = {
                    Text(text = stringResource(R.string.settings_about))
                }, icon = {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                }, summary = {
                    Text(
                        text = stringResource(
                            R.string.settings_about_summary,
                            AppUtil.getVersionName()
                        )
                    )
                }, onClick = {
                    navigate(Routes.SETTINGS_ABOUT)
                })
            }
        }
    }

}