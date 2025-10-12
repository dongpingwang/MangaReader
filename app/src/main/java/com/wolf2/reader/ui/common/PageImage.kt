package com.wolf2.reader.ui.common

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun PageAsyncImage(
    model: ByteArray?,
    contentScale: ContentScale = ContentScale.Fit,
    bitmapConfig: Bitmap.Config? = null,
    modifier: Modifier = Modifier
) {
    val stableKey = remember(model) {
        "image_${model.contentHashCode()}"
    }
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
        .data(model)
        .memoryCacheKey(MemoryCache.Key(stableKey))
        .diskCachePolicy(CachePolicy.DISABLED)
        .crossfade(false)

    if (bitmapConfig != null) {
        imageRequestBuilder.bitmapConfig(bitmapConfig)
    }

    val imageRequest = imageRequestBuilder.build()

    AsyncImage(
        model = imageRequest,
        contentScale = contentScale,
        filterQuality = FilterQuality.High,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun PageAsyncImage(
    model: String,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = model,
        contentScale = contentScale,
        filterQuality = FilterQuality.High,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun PageAsyncImage(
    model: Uri,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = model,
        contentScale = contentScale,
        filterQuality = FilterQuality.High,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun PageImage(
    bitmap: ImageBitmap,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier = Modifier
) {
    Image(
        bitmap = bitmap,
        contentScale = contentScale,
        filterQuality = FilterQuality.High,
        contentDescription = null,
        modifier = modifier
    )
}

