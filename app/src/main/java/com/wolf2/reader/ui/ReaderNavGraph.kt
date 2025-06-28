package com.wolf2.reader.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.wolf2.reader.config.AppColor
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.AppTheme
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.browser.BrowserScreen
import com.wolf2.reader.ui.common.OnLifecycleEvent
import com.wolf2.reader.ui.detail.BookDetailScreen
import com.wolf2.reader.ui.home.HomeScreen
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.read.ChapterScreen
import com.wolf2.reader.ui.read.ReadScreen
import com.wolf2.reader.ui.search.SearchScreen
import com.wolf2.reader.ui.setting.AboutScreen
import com.wolf2.reader.ui.setting.AppearanceScreen
import com.wolf2.reader.ui.setting.DownloadScreen
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
    ComicReaderTheme(
        darkMode = darkTheme,
        amoledMode = amoledFlow.value,
        color = AppColor.fromInt(appColor.value)
    ) {
        val navController = rememberNavController()
        globalViewContext = AppViewContext(
            activity = LocalActivity.current as MainActivity,
            navController = navController,
            owner = LocalViewModelStoreOwner.current!!
        )
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
                route = "${Routes.READ}/{bookUuid}",
                arguments = listOf(navArgument("bookUuid") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val bookUuid = backStackEntry.arguments?.getString("bookUuid") ?: return@composable
                ReadScreen(bookUuid = bookUuid)
            }
            composable(
                route = "${Routes.READ_CHAPTER}/{bookUuid}",
                arguments = listOf(navArgument("bookUuid") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val bookUuid = backStackEntry.arguments?.getString("bookUuid") ?: return@composable
                ChapterScreen(bookUuid = bookUuid)
            }

            composable(Routes.BROWSER_BOOK) {
                BrowserScreen()
            }

            composable(Routes.SETTINGS) {
                SettingScreen()
            }

            composable(Routes.SETTINGS_APPEARANCE) {
                AppearanceScreen()
            }

            composable(Routes.SETTINGS_DOWNLOAD) {
                DownloadScreen()
            }

            composable(Routes.SETTINGS_ABOUT) {
                AboutScreen()
            }
        }


        OnLifecycleEvent(onDispose = {
            globalViewContext = null
        })
    }

}