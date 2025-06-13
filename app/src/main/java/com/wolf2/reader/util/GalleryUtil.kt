package com.wolf2.reader.util

import android.content.Intent
import androidx.core.content.FileProvider
import com.linxiao.framework.common.globalContext
import java.io.File

object GalleryUtil {
    fun openGallery(imagePath: String) {
        val imageFile = File(imagePath)
        val contentUri = FileProvider.getUriForFile(
            globalContext,
            "${globalContext.packageName}.fileprovider",
            imageFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        runCatching {
            globalContext.startActivity(intent)
        }.onFailure { it.printStackTrace() }
    }
}