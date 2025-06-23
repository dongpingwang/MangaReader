package com.wolf2.reader.util

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.getAbsolutePath

@RequiresApi(Build.VERSION_CODES.R)
fun requestFullStorageAccess() {
    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    val uri = ("package:" + globalContext.packageName).toUri()
    intent.setData(uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    globalContext.startActivity(intent)
}

fun isExternalStorageManager(): Boolean {
    return Environment.isExternalStorageManager()
}

fun Uri.takePersistableUriPermission(): Boolean {
    return runCatching {
        globalContext.contentResolver.takePersistableUriPermission(
            this@takePersistableUriPermission,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }.onFailure { it.printStackTrace() }.isSuccess
}

fun Uri.releasePersistableUriPermission(): Boolean {
    return runCatching {
        globalContext.contentResolver.releasePersistableUriPermission(
            this@releasePersistableUriPermission,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }.onFailure { it.printStackTrace() }.isSuccess
}

fun Uri.storagePath(): String? {
    return DocumentFileCompat.fromUri(globalContext, this)?.getAbsolutePath(globalContext)
}