package com.wolf2.reader.ui.detail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.book.Book

@Composable
internal fun BookMetadata(
    book: Book,
    favorite: Boolean,
    onTitleChange: (String) -> Unit = {},
    onAuthorChange: (String) -> Unit = {},
    onFavoriteChange: (Boolean) -> Unit = {}
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var selected by remember { mutableStateOf(favorite) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(108.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = book.cover.getImageSource(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(120.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotEmpty()) {
                        onTitleChange(it)
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
            HorizontalDivider(thickness = 1.5F.dp, color = MaterialTheme.colorScheme.primary)

            BasicTextField(
                value = author,
                onValueChange = {
                    author = it
                    if (it.isNotEmpty()) {
                        onAuthorChange(it)
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(top = 8.dp)
            )
            HorizontalDivider(thickness = 1.5F.dp, color = MaterialTheme.colorScheme.primary)

            FilterChip(
                onClick = {
                    selected = !selected
                    onFavoriteChange(selected)
                },
                label = {
                    Text(
                        text = if (selected) stringResource(R.string.collected) else stringResource(
                            R.string.collect_book
                        )
                    )
                },
                selected = selected,
                leadingIcon =
                    {
                        val icon =
                            if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}