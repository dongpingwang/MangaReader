package com.wolf2.reader.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wolf2.reader.MainActivity
import com.wolf2.reader.AppViewContext
import com.wolf2.reader.constant.AppColor
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.constant.AppTheme
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.browser.BrowserScreen
import com.wolf2.reader.ui.detail.BookDetailScreen
import com.wolf2.reader.ui.home.HomeScreen
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.preview.ImagePreviewScreen
import com.wolf2.reader.ui.read.MangaReadScreen
import com.wolf2.reader.ui.search.SearchScreen
import com.wolf2.reader.ui.setting.AboutScreen
import com.wolf2.reader.ui.setting.AppearanceScreen
import com.wolf2.reader.ui.setting.SafScreen
import com.wolf2.reader.ui.setting.ReaderScreen
import com.wolf2.reader.ui.setting.SettingScreen
import com.wolf2.reader.ui.theme.ComicReaderTheme

@Composable
fun ReaderNavGraph() {
    val themeMode = AppConfig.themeMode.collectAsState()
    val darkTheme = when (AppTheme.fromInt(themeMode.value)) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    val appColor = AppConfig.appColor.collectAsState()
    val amoledFlow = AppConfig.amoled.collectAsState()

    val navController = rememberNavController()
    globalViewContext = AppViewContext(
        activity = LocalActivity.current as MainActivity,
        navController = navController,
        owner = LocalViewModelStoreOwner.current!!
    )
    DisposableEffect(Unit) {
        object : DisposableEffectResult {
            override fun dispose() {
                globalViewContext = null
            }
        }
    }

    ComicReaderTheme(
        darkMode = darkTheme,
        amoledMode = amoledFlow.value,
        color = AppColor.fromInt(appColor.value)
    ) {
        NavHost(
            navController = navController, startDestination = Routes.HOME,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            composable(Routes.HOME) {
                HomeScreen()
            }
            composable(Routes.SEARCH) {
                SearchScreen()
            }
            composable(
                route = "${Routes.BOOK_DETAIL}/{bookUuid}",
                arguments = listOf(navArgument("bookUuid") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val bookUuid = backStackEntry.arguments?.getString("bookUuid") ?: return@composable
                BookDetailScreen(bookUuid = bookUuid)
            }
            composable(
                route = "${Routes.READ}/{bookUuid}/{from}/{curPage}",
                arguments = listOf(navArgument("bookUuid") {
                    type = NavType.StringType
                }, navArgument("from") {
                    type = NavType.StringType
                }, navArgument("curPage") {
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                val bookUuid = backStackEntry.arguments?.getString("bookUuid") ?: return@composable
                val from = backStackEntry.arguments?.getString("from") ?: return@composable
                val curPage = backStackEntry.arguments?.getInt("curPage")
                MangaReadScreen(bookUuid = bookUuid, from = from, curPage = curPage)
            }

            composable(Routes.BROWSER_BOOK) {
                BrowserScreen()
            }

            composable(Routes.IMAGE_PREVIEW) {
                ImagePreviewScreen()
            }

            composable(Routes.SETTINGS) {
                SettingScreen()
            }

            composable(Routes.SETTINGS_APPEARANCE) {
                AppearanceScreen()
            }

            composable(Routes.SETTINGS_SAF) {
                SafScreen()
            }

            composable(Routes.SETTINGS_ABOUT) {
                AboutScreen()
            }

            composable(Routes.SETTINGS_READER) {
                ReaderScreen()
            }
        }
    }
}