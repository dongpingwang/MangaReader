package com.wolf2.reader.util


object AppUtil {

    fun getVersionCode(): Int {
        return globalContext.packageManager.getPackageInfo(globalContext.packageName, 0).versionCode
    }

    fun getVersionName(): String {
        return globalContext.packageManager.getPackageInfo(globalContext.packageName, 0).versionName
            ?: ""
    }

}