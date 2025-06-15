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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.wolf2.reader.R
import com.wolf2.reader.popBackStack
import com.wolf2.reader.util.AppUtil
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import androidx.core.net.toUri
import com.wolf2.reader.util.SystemAppUtil

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.settings_about))
        }, navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack, contentDescription = null
                )
            }
        })

        val sourceUrl = stringResource(R.string.settings_source_url)

        ProvidePreferenceLocals {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                preference(
                    key = "about_version_code",
                    title = {
                        Text(
                            text = stringResource(
                                R.string.settings_version_name,
                                AppUtil.getVersionName()
                            )
                        )
                    },
                    summary = {
                        Text(
                            text = stringResource(
                                R.string.settings_version_code,
                                AppUtil.getVersionCode()
                            )
                        )
                    },
                    onClick = {}
                )

                preference(
                    key = "about_source_code",
                    title = {
                        Text(
                            text = stringResource(R.string.settings_source)
                        )
                    },
                    summary = {
                        Text(
                            text = sourceUrl,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        SystemAppUtil.openBrowser(sourceUrl.toUri())
                    }
                )
            }

        }
    }

}