package com.wolf2.reader.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wolf2.reader.MainActivity
import com.wolf2.reader.AppViewContext
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.ui.browser.BrowserScreen
import com.wolf2.reader.ui.detail.BookDetailScreen
import com.wolf2.reader.ui.home.HomeScreen
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.ui.read.ChapterScreen
import com.wolf2.reader.ui.read.ReadScreen
import com.wolf2.reader.ui.search.SearchScreen
import com.wolf2.reader.ui.theme.ComicReaderTheme

@Composable
fun ReaderNavGraph() {
    ComicReaderTheme {
        val navController = rememberNavController()
        globalViewContext = AppViewContext(
            activity = LocalActivity.current as MainActivity,
            navController = navController,
            owner = LocalViewModelStoreOwner.current!!
        )
        NavHost(navController = navController, startDestination = Routes.HOME) {
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
            ) {
                BookDetailScreen()
            }
            composable(
                route = "${Routes.READ}/{bookUuid}",
                arguments = listOf(navArgument("bookUuid") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val bookUuid =
                    backStackEntry.arguments?.getString("bookUuid") ?: return@composable
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
        }

        DisposableEffect(Unit) {
            onDispose { globalViewContext = null }
        }
    }

}