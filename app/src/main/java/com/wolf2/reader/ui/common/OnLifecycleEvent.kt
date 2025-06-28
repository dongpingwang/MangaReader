package com.wolf2.reader.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import timber.log.Timber


fun Lifecycle.Event.isOnDestroy(): Boolean {
    return this == Lifecycle.Event.ON_DESTROY
}


@Composable
fun OnLifecycleEvent(onEvent: (event: Lifecycle.Event) -> Unit = {}, onDispose: () -> Unit = {}) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Timber.d("onEvent==>$event")
            onEvent(event)
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            onDispose()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}