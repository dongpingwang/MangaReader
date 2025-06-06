package com.wolf2.reader.util

import androidx.lifecycle.MutableLiveData
import com.tencent.mmkv.MMKV

interface PreferenceConvert<T> {
    fun set(data: T): String
    fun get(data: String): T
}

class PreferenceLiveData<T>(
    private val key: String,
    private val defValue: T,
    private val convert: PreferenceConvert<T>
) :
    MutableLiveData<T>() {

    init {
        val data = MMKV.defaultMMKV().decodeString(key, null)
        value = if (data == null) defValue else convert.get(data)
    }

    override fun postValue(value: T) {
        super.postValue(value)
        MMKV.defaultMMKV().encode(key, convert.set(value))
    }

    override fun getValue(): T {
        return super.getValue() ?: defValue
    }
}