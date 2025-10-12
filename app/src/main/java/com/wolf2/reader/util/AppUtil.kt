package com.wolf2.reader.util

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build

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

    fun shareFile(uri: Uri, fileType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = fileType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                clipData = ClipData.newRawUri(null, uri)
            }
        }
        globalContext.startActivity(chooserIntent)
    }
}