package com.wolf2.reader.util

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object AppUtil {

    fun getVersionCode(): Int {
        return globalContext.packageManager.getPackageInfo(globalContext.packageName, 0).versionCode
    }

    fun getVersionName(): String {
        return globalContext.packageManager.getPackageInfo(globalContext.packageName, 0).versionName
            ?: ""
    }

    fun openBrowser(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setData(uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        runCatching {
            globalContext.startActivity(intent)
        }.onFailure { it.printStackTrace() }
    }


    fun shareFile(path: String, fileType: String) {
        val uri = FileProvider.getUriForFile(
            globalContext,
            globalContext.packageName + ".fileprovider",
            File(path)
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            setType(fileType)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            globalContext.startActivity(Intent.createChooser(intent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            })
        }.onFailure { it.printStackTrace() }
    }
}