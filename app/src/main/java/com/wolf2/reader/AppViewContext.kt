package com.wolf2.reader

import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import timber.log.Timber

data class AppViewContext(
    val activity: MainActivity,
    val navController: NavHostController,
    val owner: ViewModelStoreOwner
)

var globalViewContext: AppViewContext? = null
    set(value) {
        field = value
        Timber.d("set AppViewContext: $value")
    }

val popBackStack: () -> Unit = {
    val previous = globalViewContext?.navController?.previousBackStackEntry
    if (previous != null) {
        globalViewContext?.navController?.popBackStack()
    }
}

val navigate: (String) -> Unit = {
    globalViewContext?.navController?.navigate(route = it)
}

fun finishMainActivity() {
    globalViewContext?.activity?.finish()
}