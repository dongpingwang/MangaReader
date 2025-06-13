package com.wolf2.reader.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Delete
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
import com.wolf2.reader.popBackStack
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import com.wolf2.reader.config.Constants
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.twoTargetIconButtonPreference

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDownloadScreen() {
    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.settings_download))
        }, navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = null
                )
            }
        })

        val dirRoot = Constants.dirRoot

        ProvidePreferenceLocals {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                twoTargetIconButtonPreference(
                    key = "download_dir",
                    title = {
                        Text(
                            text = stringResource(R.string.settings_cache_dir)
                        )
                    },
                    iconButtonIcon = {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                    },
                    summary = {
                        Text(text = dirRoot)
                    },
                    onClick = {},
                    onIconButtonClick = {

                    }
                )

                switchPreference(
                    key = "cover_image_extension",
                    defaultValue = false,
                    title = {
                        Text(text = stringResource(R.string.settings_cover_image_extension))
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_cover_image_extension_summary))
                    }
                )
            }

        }
    }

}