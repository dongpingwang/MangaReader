package com.wolf2.reader.util

import android.widget.Toast
import com.linxiao.framework.common.globalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

object ToastUtil {
    private var toast: WeakReference<Toast>? = null

    suspend fun toastOnUiThread(msg: String) = withContext(Dispatchers.Main) {
        toast?.get()?.cancel()
        val t = Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT)
        toast = WeakReference(t)
        t.show()
    }
}