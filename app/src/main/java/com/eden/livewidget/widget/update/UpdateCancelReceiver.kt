package com.eden.livewidget.widget.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.eden.livewidget.util.goAsync
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class UpdateCancelReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(this.javaClass.name, "Context is null")
            return
        }

        val appWidgetId = intent?.getIntExtra(UpdateScheduler.APP_WIDGET_ID, -1) ?: -1

        UpdateScheduler.cancelCurrentRequest(context, appWidgetId)

        @OptIn(DelicateCoroutinesApi::class)
        goAsync(GlobalScope, Dispatchers.Default) {
            updateWidget(context, appWidgetId, -1)
        }
    }
}