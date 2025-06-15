package com.wolf2.reader

import android.app.Application
import com.bobbyesp.crashhandler.CrashHandler.setupCrashHandler
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.util.traceMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ReaderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val app = this
        CoroutineScope(Dispatchers.IO).launch {
            traceMillis {
                Timber.plant(Timber.DebugTree())
                Timber.d("create")
                DatabaseHelper.init(app)
                setupCrashHandler {
                    globalViewContext?.activity?.finish()
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.d("terminate")
    }
}