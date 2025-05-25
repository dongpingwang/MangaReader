package com.wolf2.reader.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.linxiao.framework.common.globalContext
import java.lang.ref.WeakReference

object ToastUtil {
    private var toast: WeakReference<Toast>? = null
    private val h = Handler(Looper.getMainLooper())

    fun toast(msg: String) {
        toast?.get()?.cancel()
        val t = Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT)
        toast = WeakReference(t)
        if (Looper.myLooper() == h.looper) {
            t.show()
        } else {
            h.post { t.show() }
        }
    }
}