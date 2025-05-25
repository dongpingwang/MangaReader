package com.wolf2.reader.ui.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun SettingScreen() {

    Box(modifier = Modifier.fillMaxSize()) {
        Text("setting", modifier = Modifier.align(Alignment.Center))
    }
}