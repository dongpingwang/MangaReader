package com.wolf2.reader.util

import android.content.Intent
import android.net.Uri
import com.linxiao.framework.common.globalContext


object BrowserUtil {

    fun openBrowser(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setData(uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        runCatching {
            globalContext.startActivity(intent)
        }.onFailure { it.printStackTrace() }
    }
}