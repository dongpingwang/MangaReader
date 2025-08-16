package com.wolf2.reader.ui.read.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun PageAsyncImage(model: Any?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = model,
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.High,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun PageImage(bitmap: ImageBitmap, modifier: Modifier = Modifier) {
    Image(
        bitmap = bitmap,
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.High,
        contentDescription = null,
        modifier = modifier
    )
}

