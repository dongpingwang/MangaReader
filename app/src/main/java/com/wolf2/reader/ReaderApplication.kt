package com.wolf2.reader

import android.app.Application
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.util.traceMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ReaderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            traceMillis {
                Timber.plant(Timber.DebugTree())
                DatabaseHelper.init()
                Timber.d("app created")
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.d("app terminated")
    }
}