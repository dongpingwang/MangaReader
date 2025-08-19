package com.wolf2.reader.ui.common.chapter

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.read.component.PageImage

@Composable
fun CardImage(
    pageIndex: Int,
    pageContent: PageContent,
    onPageChange: (Int) -> Unit,
    onLoadImage: (PageContent) -> Bitmap,
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
        PageImage(
            bitmap = onLoadImage(pageContent).asImageBitmap(),
            modifier = Modifier
                .fillMaxSize()
        )
    }
}