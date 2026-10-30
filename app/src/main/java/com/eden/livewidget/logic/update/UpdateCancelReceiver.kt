package com.eden.livewidget.logic.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateCancelReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(this.javaClass.name, "Context is null")
            return
        }

        val appWidgetId = intent?.getIntExtra(UpdateScheduler.APP_WIDGET_ID, -1) ?: -1

        UpdateScheduler.cancelCurrentRequest(context, appWidgetId)

        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            try {
                updateWidget(context, appWidgetId, -1)
            }
            finally {
                pendingResult.finish()
            }
        }
    }
}