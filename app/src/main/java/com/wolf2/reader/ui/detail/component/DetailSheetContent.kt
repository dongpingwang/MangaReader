package com.wolf2.reader.ui.detail.component

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wolf2.reader.R
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.common.chapter.HeadButtons
import com.wolf2.reader.ui.common.chapter.SheetContent
import com.wolf2.reader.ui.detail.DetailUiState
import com.wolf2.reader.util.LoadResult
import me.saket.cascade.CascadeDropdownMenu

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailSheetContent(
    uiState: DetailUiState,
    onNavigationToRead: () -> Unit,
    onCopyContent: () -> Unit,
    onDeleteReadRecord: () -> Unit,
    onShareBookFile: () -> Unit,
    onPageChange: (Int) -> Unit,
    onLoadBuffer: (PageContent) -> ByteArray?,
    onLoadBitmap: (PageContent) -> Bitmap?,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val loadComplete = uiState.bookResult is LoadResult.Success

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        var sblClicked by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            HeadButtons(selectedIndex = selectedIndex, onSelectChange = { selectedIndex = it })

            Spacer(modifier = Modifier.weight(1F))

            Box(modifier = Modifier.wrapContentSize()) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(
                            onClick = onNavigationToRead,
                        ) {
                            Text(text = stringResource(R.string.sbl_read))
                        }
                    },
                    trailingButton = {
                        SplitButtonDefaults.TrailingButton(
                            checked = sblClicked,
                            onCheckedChange = { sblClicked = it },
                        ) {
                            val rotation by animateFloatAsState(targetValue = if (sblClicked) 180f else 0f)
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                modifier =
                                Modifier
                                    .size(SplitButtonDefaults.TrailingIconSize)
                                    .graphicsLayer {
                                        this.rotationZ = rotation
                                    },
                                contentDescription = null
                            )
                        }
                    }
                )

                if (sblClicked) {
                    SblDropMenu(
                        onCopyContent = onCopyContent,
                        onDeleteReadRecord = onDeleteReadRecord,
                        onShareBookFile = onShareBookFile,
                        onDismissRequest = {
                            sblClicked = false
                        })
                }
            }
        }

        if (loadComplete) {
            val book = (uiState.bookResult as LoadResult.Success).data
            SheetContent(
                selectedIndex = selectedIndex,
                chapters = book.chapters,
                pageContents = book.pageContents,
                bookMarks = uiState.bookMarks,
                onSelectChange = { selectedIndex = it },
                onPageChange = onPageChange,
                onLoadBuffer = onLoadBuffer,
                onLoadBitmap = onLoadBitmap
            )
        }
    }
}

@Composable
private fun SblDropMenu(
    onCopyContent: () -> Unit,
    onDeleteReadRecord: () -> Unit,
    onShareBookFile: () -> Unit,
    onDismissRequest: () -> Unit
) {
    CascadeDropdownMenu(expanded = true, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_content_copy)) },
            onClick = {
                onCopyContent()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_delete_read_record)) },
            onClick = {
                onDeleteReadRecord()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.sbl_menu_share)) },
            onClick = {
                onShareBookFile()
                onDismissRequest()
            }
        )
    }
}