package com.wolf2.reader.util

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import java.util.regex.Pattern

private const val TAG = "MethodTraceKt"
private const val THRESHOLD_MS = 200

@SuppressLint("LogNotTimber")
fun <T> traceMillis(block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    val end = System.currentTimeMillis()
    val cost = end - start
    val callMethod = _callMethod
    val methodName = "${callMethod.first}#${callMethod.second}()"

    if (cost > THRESHOLD_MS) {
        Log.d(TAG, "$methodName cost ${cost}ms on ${Thread.currentThread().name} thread, it exceed ${THRESHOLD_MS}ms!")
    } else {
        Log.d(TAG, "$methodName cost ${cost}ms on ${Thread.currentThread().name} thread.")

    }
    return result
}

private val _callMethod: Pair<String, String>
    get() = Throwable().stackTrace
        .first { it.className !in fqcnIgnore }
        .let(::createStackElementTag)

private const val MAX_TAG_LENGTH = 23
private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
private val fqcnIgnore = listOf("com.arale.comicreader.util.MethodTraceKt")

private fun createStackElementTag(element: StackTraceElement): Pair<String, String> {
    var tag = element.className.substringAfterLast('.')
    val m = ANONYMOUS_CLASS.matcher(tag)
    if (m.find()) {
        tag = m.replaceAll("")
    }
    return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
        tag
    } else {
        tag.substring(0, MAX_TAG_LENGTH)
    } to (element.methodName)
}


