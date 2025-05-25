package com.wolf2.reader

import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController

data class AppViewContext(
    val activity: MainActivity,
    val navController: NavHostController,
    val owner: ViewModelStoreOwner
)

var globalViewContext: AppViewContext? = null