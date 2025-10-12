package com.wolf2.reader.ui.read.component

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.PageAsyncImage
import com.wolf2.reader.ui.common.PageImage

@Composable
fun ReaderPageImage(
    pageContent: PageContent,
    onLoadImage: (PageContent) -> Bitmap,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier = Modifier
) {
//    if (pageContent.imageUri != Uri.EMPTY) {
//        PageAsyncImage(
//            model = pageContent.imageUri,
//            contentScale = contentScale,
//            modifier = modifier
//        )
//    } else {
//        PageImage(
//            bitmap = onLoadImage(pageContent).asImageBitmap(),
//            contentScale = contentScale,
//            modifier = modifier
//        )
//    }

    PageImage(
        bitmap = onLoadImage(pageContent).asImageBitmap(),
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
fun ReaderPageAsyncImage(
    pageContent: PageContent,
    onLoadBuffer: (PageContent) -> ByteArray?,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier = Modifier
) {
//    if (pageContent.imageUri != Uri.EMPTY) {
//        PageAsyncImage(
//            model = pageContent.imageUri,
//            contentScale = contentScale,
//            modifier = modifier
//        )
//    } else {
//        PageAsyncImage(
//            model = onLoadBuffer(pageContent),
//            contentScale = contentScale,
//            modifier = modifier
//        )
//    }
    PageAsyncImage(
        model = onLoadBuffer(pageContent),
        contentScale = contentScale,
        modifier = modifier
    )
}