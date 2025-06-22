package com.wolf2.reader.util

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import androidx.core.net.toUri

object SystemAppUtil {

    fun openBrowser(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setData(uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        runCatching {
            globalContext.startActivity(intent)
        }.onFailure { it.printStackTrace() }
    }

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

    fun startAccessSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        val uri = ("package:" + globalContext.getPackageName()).toUri()
        intent.setData(uri)
        intent.setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$AppManageExternalStorageActivity"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        runCatching {
            globalContext.startActivity(intent)
        }.onFailure {
            it.printStackTrace()
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            globalContext.startActivity(intent)
        }
    }
}