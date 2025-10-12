package com.wolf2.reader.util

import android.content.Intent
import android.content.UriPermission
import android.net.Uri
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.getSimplePath

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

fun getPersistedUriPermissions(): List<UriPermission> {
    return globalContext.contentResolver.persistedUriPermissions
}

fun Uri.checkUriPermissions(): Boolean {
    return DocumentFileCompat.fromUri(globalContext, this)?.canRead() == true
}

fun Uri.storagePath(): String? {
    return DocumentFileCompat.fromUri(globalContext, this)?.getSimplePath(globalContext)
}