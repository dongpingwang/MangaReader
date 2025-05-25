package com.wolf2.reader.util

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.saveable.rememberSaveable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object FileUtil {
    fun getParentDir(filePath: String): String {
        val lastSlashIndex = filePath.lastIndexOf("/")
        return if (lastSlashIndex != -1) filePath.substring(0, lastSlashIndex) else ""
    }

    fun getDisplayDate(lastModified: Long): String {
        return SimpleDateFormat("yyyy/MMM/dd HH:mm", Locale.getDefault()).format(Date(lastModified))
    }

    fun getDisplaySize(length: Long): String {
        val sizeBytes = length
        val fileSizeKB = if (sizeBytes > 0) sizeBytes.toDouble() / 1024.0 else 0.0
        val fileSizeMB = if (sizeBytes > 0) fileSizeKB / 1024.0 else 0.0
        return if (fileSizeMB >= 1.0) "%.2f MB".format(fileSizeMB)
        else if (fileSizeMB > 0.0) "%.2f KB".format(fileSizeKB)
        else "0 KB"
    }
}