package com.wolf2.reader.util

import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
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

fun DocumentFile.listFilesCount(): Int {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
        this.uri,
        DocumentsContract.getDocumentId(this.uri)
    )
    var c: Cursor? = null
    var count = 0
    try {
        c = globalContext.contentResolver.query(
            childrenUri,
            arrayOf("document_id"),
            null as String?,
            null as Array<String?>?,
            null as String?
        )
        count = c?.count ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        c?.closeQuietly()
    }
    return count
}

fun DocumentFile.listFilesUri(): List<Uri> {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
        this.uri,
        DocumentsContract.getDocumentId(this.uri)
    )
    var c: Cursor? = null
    val results = mutableListOf<Uri>()
    try {
        c = globalContext.contentResolver.query(
            childrenUri,
            arrayOf("document_id"),
            null,
            null,
            null
        )
        while (c!!.moveToNext()) {
            val documentId = c.getString(0)
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(this.uri, documentId)
            results.add(documentUri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        c?.closeQuietly()
    }
    return results
}

// todo 判断是否包含.  **不准确**，通过DocumentFile判断太慢
fun Uri.isDirectoryEarly(): Boolean {
    return this.path?.contains(".") == false
}

fun Uri.isDirectory(): Boolean {
    return isDirectory(globalContext, this)
}

private fun isDirectory(context: Context, self: Uri): Boolean {
    return "vnd.android.document/directory" == getRawType(context, self)
}

private fun getRawType(context: Context, self: Uri): String? {
    return queryForString(context, self, "mime_type", null)
}

private fun queryForString(
    context: Context,
    self: Uri,
    column: String,
    defaultValue: String?
): String? {
    val resolver = context.contentResolver
    var c: Cursor? = null
    try {
        c = resolver.query(
            self,
            arrayOf(column),
            null as String?,
            null as Array<String?>?,
            null as String?
        )
        if (c!!.moveToFirst() && !c.isNull(0)) {
            val result = c.getString(0)
            return result
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        c?.closeQuietly()
    }
    return defaultValue
}