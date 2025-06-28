package com.wolf2.reader

import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController

data class AppViewContext(
    val activity: MainActivity,
    val navController: NavHostController,
    val owner: ViewModelStoreOwner
)

var globalViewContext: AppViewContext? = null

val popBackStack: () -> Unit = {
    val previous = globalViewContext?.navController?.previousBackStackEntry
    if (previous != null) {
        globalViewContext?.navController?.popBackStack()
    }
}

val navigate: (String) -> Unit = {
    globalViewContext?.navController?.navigate(route = it)
}

fun currentRoute(): String? {
    return globalViewContext?.navController?.currentDestination?.route
}