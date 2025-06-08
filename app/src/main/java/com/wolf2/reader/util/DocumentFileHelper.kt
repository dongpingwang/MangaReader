package com.wolf2.reader.util

import android.content.Intent
import android.content.UriPermission
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.linxiao.framework.common.globalContext
import com.wolf2.reader.config.supportFileExtends

data class DocumentFileExt(
    val file: DocumentFile, // 只会是fileUri，不是treeUri
    val path: String,
    val selected: Boolean = false
)

fun getPersistedUriPermissions(): List<UriPermission> {
    return globalContext.contentResolver?.persistedUriPermissions.let { permissions ->
        if (permissions.isNullOrEmpty()) return@let emptyList()
        permissions.sortedBy { it.uri.path?.lowercase() }
    }
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


fun listFilesFromTreeUri(treeUri: Uri): List<DocumentFileExt> {
    fun listFilesRecursively(dir: DocumentFile, result: MutableList<DocumentFileExt>) {
        val files = dir.listFiles()
        if (files.isNullOrEmpty()) return
        for (file in files) {
            if (file.isDirectory) {
                listFilesRecursively(file, result)
            } else {
                file.uri.filePathFromFileUri()?.let { path ->
                    supportFileExtends.forEach { extend ->
                        if (path.endsWith(extend, ignoreCase = true)) {
                            result.add(DocumentFileExt(file = file, path = path))
                            return@let
                        }
                    }
                }
            }
        }
    }
    val result = mutableListOf<DocumentFileExt>()
    val root = DocumentFile.fromTreeUri(globalContext, treeUri)
    if (root != null && root.exists()) {
        listFilesRecursively(root, result)
    }
    return result
}

fun Uri.filePathFromFileUri(): String? {
    val scheme = this.scheme ?: return null
    val queryData = object : () -> String? {
        override fun invoke(): String? {
            return runCatching {
                val docIds = DocumentsContract.getDocumentId(this@filePathFromFileUri).split(":")
                when (docIds[0]) {
                    "primary" -> Environment.getExternalStorageDirectory().absolutePath + "/" + docIds[1]
                    else -> null
                }
            }.onFailure { it.printStackTrace() }.getOrNull()
        }
    }
    return when (scheme) {
        "file" -> this.path
        "content" -> queryData.invoke()
        else -> null
    }
}