package com.wolf2.reader.ui.setting

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wolf2.reader.R
import com.wolf2.reader.constant.AppColor
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.mmkvEmit
import com.wolf2.reader.popBackStack
import com.wolf2.reader.ui.theme.DarkBlueColors
import com.wolf2.reader.ui.theme.DarkGreenColors
import com.wolf2.reader.ui.theme.DarkOrangeColors
import com.wolf2.reader.ui.theme.DarkPurpleColors
import com.wolf2.reader.ui.theme.DarkRedColors
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen() {
    val themeModeFlow = AppConfig.themeMode.collectAsStateWithLifecycle()
    val themeDescs = stringArrayResource(R.array.theme_modes).toList()
    val curThemeMode = themeDescs[themeModeFlow.value]

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

    val navLabelShowFlow = AppConfig.navLabelShow.collectAsStateWithLifecycle()
    val navLabelShowDescs = stringArrayResource(R.array.nav_label_show).toList()
    val curNavLabelShow = navLabelShowDescs[navLabelShowFlow.value]

    Column {
        TopAppBar(title = {
            Text(text = stringResource(R.string.settings_appearance))
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
                                    AppConfig.appColor.mmkvEmit(i)
                                }
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                    }
                }

                item {
                    ListPreference(
                        value = curThemeMode,
                        onValueChange = {
                            AppConfig.themeMode.mmkvEmit(themeDescs.indexOf(it))
                        },
                        values = themeDescs,
                        title = {
                            Text(text = stringResource(R.string.settings_theme_mode))
                        },
                        summary = {
                            Text(text = curThemeMode)
                        },
                        type = ListPreferenceType.DROPDOWN_MENU
                    )
                }
                item {
                    SwitchPreference(
                        value = amoled.value,
                        onValueChange = {
                            AppConfig.amoled.mmkvEmit(it)
                        },
                        title = {
                            Text(text = stringResource(R.string.settings_dark_amoled_mode))
                        },
                        summary = {
                            Text(text = stringResource(R.string.settings_dark_amoled_mode_summary))
                        }
                    )
                }

                item {
                    HorizontalDivider()
                }

                item {
                    ListPreference(
                        value = curNavLabelShow,
                        onValueChange = {
                            AppConfig.navLabelShow.mmkvEmit(navLabelShowDescs.indexOf(it))
                        },
                        values = navLabelShowDescs,
                        title = {
                            Text(text = stringResource(R.string.settings_nav_label_show))
                        },
                        summary = {
                            Text(text = curNavLabelShow)
                        },
                        type = ListPreferenceType.DROPDOWN_MENU
                    )
                }
            }
        }
    }
}


