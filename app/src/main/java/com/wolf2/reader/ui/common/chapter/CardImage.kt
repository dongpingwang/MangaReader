package com.wolf2.reader.ui.common.chapter

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.PageAsyncImage

@Composable
fun CardImage(
    pageIndex: Int,
    pageContent: PageContent,
    onPageChange: (Int) -> Unit,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onLoadBitmap: (PageContent) -> Bitmap?,
) {
    OutlinedCard(
        onClick = { onPageChange(pageIndex) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxSize()
    ) {
        if (pageContent.imageUri != Uri.EMPTY) {
            PageAsyncImage(
                model = pageContent.imageUri,
                modifier = Modifier
                    .fillMaxSize()
            )
        } else if (pageContent.pageIndex >= 0) {
            PageAsyncImage(
                model = onLoadBitmap(pageContent),
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            PageAsyncImage(
                model = onLoadBuffer(pageContent),
                bitmapConfig = Bitmap.Config.ALPHA_8,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}