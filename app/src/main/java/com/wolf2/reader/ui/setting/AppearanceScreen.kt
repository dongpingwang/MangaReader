package com.wolf2.reader.ui.setting

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wolf2.reader.R
import com.wolf2.reader.config.AppColor
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.popBackStack
import com.yangdai.opennote.presentation.theme.DarkBlueColors
import com.yangdai.opennote.presentation.theme.DarkGreenColors
import com.yangdai.opennote.presentation.theme.DarkOrangeColors
import com.yangdai.opennote.presentation.theme.DarkPurpleColors
import com.yangdai.opennote.presentation.theme.DarkRedColors
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen() {
    var showThemeChooseDialog by remember { mutableStateOf(false) }
    val themeMode = AppConfig.themeMode.collectAsStateWithLifecycle()
    val themeDesc = stringArrayResource(R.array.theme_modes)[themeMode.value]
    val amoled = AppConfig.amoled.collectAsStateWithLifecycle()
    val appColor = AppConfig.appColor.collectAsStateWithLifecycle()
    val colorSchemes = listOf(
        Pair(AppColor.DYNAMIC, dynamicDarkColorScheme(LocalContext.current)),
        Pair(AppColor.PURPLE, DarkPurpleColors),
        Pair(AppColor.BLUE, DarkBlueColors),
        Pair(AppColor.GREEN, DarkGreenColors),
        Pair(AppColor.ORANGE, DarkOrangeColors),
        Pair(AppColor.RED, DarkRedColors)
    )
    val colorSchemeTexts = stringArrayResource(R.array.color_scheme_descriptions)
    val hapticFeedback = LocalHapticFeedback.current

    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.settings_appearance))
        }, navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = null
                )
            }
        })

        ProvidePreferenceLocals {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.settings_color_scheme))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.Start
                            )
                        ) {
                            colorSchemes.fastForEachIndexed { i, colorSchemePair ->
                                SelectableColorPlatte(
                                    selected = AppColor.fromInt(appColor.value) == colorSchemePair.first,
                                    colorScheme = colorSchemePair.second,
                                    desc = colorSchemeTexts[i]
                                ) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    AppConfig.appColor.value = i
                                }
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                    }
                }

                preference(
                    key = "theme_mode", title = {
                        Text(text = stringResource(R.string.settings_theme_mode))
                    }, summary = {
                        Text(text = themeDesc)
                    },
                    onClick = {
                        showThemeChooseDialog = true
                    }
                )
                item {
                    SwitchPreference(
                        value = amoled.value,
                        onValueChange = {
                            AppConfig.amoled.value = it
                        },
                        title = {
                            Text(text = stringResource(R.string.settings_dark_amoled_mode))
                        },
                        summary = {
                            Text(text = stringResource(R.string.settings_dark_amoled_mode_summary))
                        }
                    )
                }
            }

        }
    }

    if (showThemeChooseDialog) {
        ThemeChooseDialog(
            themeMode = themeMode.value,
            onThemeModeChange = {
                AppConfig.themeMode.value = it
            },
            onDismissRequest = {
                showThemeChooseDialog = false
            })
    }
}

@Composable
private fun ThemeChooseDialog(
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    val modes = stringArrayResource(R.array.theme_modes)
    var curMode by remember { mutableIntStateOf(themeMode) }

    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = {
            onDismissRequest()
            if (themeMode != curMode) {
                onThemeModeChange(curMode)
            }
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, title = {
        Text(stringResource(R.string.settings_theme_mode))
    }, text = {
        Column(Modifier.selectableGroup()) {
            modes.forEachIndexed { i, text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = curMode == i,
                            onClick = {
                                curMode = i
                            },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = curMode == i,
                        onClick = null
                    )
                    Text(
                        text = text,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    })
}


