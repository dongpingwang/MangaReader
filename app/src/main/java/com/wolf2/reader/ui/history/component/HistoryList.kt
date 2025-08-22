package com.wolf2.reader.ui.history.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.ReadTime
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.reader.durations
import com.wolf2.reader.ui.common.EmptyHint
import com.wolf2.reader.util.DateUtil

@Composable
fun HistoryList(
    books: List<Book>,
    readTimes: List<ReadTime>,
    onNavigationToHome: () -> Unit,
    onNavigationToDetail: (String) -> Unit,
) {
    if (readTimes.isEmpty()) {
        EmptyHint(hint = stringResource(R.string.empty_history_hint), onAction = onNavigationToHome)
        return
    }
    // 按日期分组
    val dateMap = readTimes.groupBy { it.date }
    LazyColumn {
        dateMap.keys.forEach { date ->
            item {
                Text(text = DateUtil.compareDate(date), color = MaterialTheme.colorScheme.primary)
            }
            val rtsByDate =
                dateMap[date]?.sortedByDescending { it.endReadTimeMillis } ?: return@forEach
            // 按书籍分组
            val bookMap = rtsByDate.groupBy { it.bookUuid }

            bookMap.keys.forEach { bookUuid ->
                val booksOfDate =
                    bookMap[bookUuid]?.sortedByDescending { it.endReadTimeMillis } ?: return@forEach
                val book = books.find { it.uuid == bookUuid } ?: return@forEach
                val todayOfReadTime = DateUtil.getMinutes(booksOfDate.durations()).coerceAtLeast(1)
                val allOfReadTime =
                    DateUtil.getMinutes(readTimes.filter { it.bookUuid == bookUuid }.durations())
                        .coerceAtLeast(1)
                item {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { onNavigationToDetail(book.uuid) }
                    ) {
                        Row {
                            AsyncImage(
                                model = book.cover.getImageSource(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit,
                            )
                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(
                                        R.string.history_read_time_desc,
                                        todayOfReadTime,
                                        allOfReadTime
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}