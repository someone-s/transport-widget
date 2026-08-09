package com.eden.livewidget.widget.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.eden.livewidget.util.goAsync
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlin.time.Duration.Companion.seconds

class UpdateExecuteReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) {
            Log.e(this.javaClass.name, "Context is null")
            return
        }

        val appWidgetId = intent?.getIntExtra(UpdateScheduler.APP_WIDGET_ID, -1) ?: -1

        val remainingTimes = intent?.getIntExtra(UpdateScheduler.REMAINING_TIMES, -1) ?: -1

        @OptIn(DelicateCoroutinesApi::class)
        goAsync(MainScope(), Dispatchers.Default) {

            val success = try {
                updateWidget(context, appWidgetId, remainingTimes)
            }
            catch (e: Exception) {
                Log.e(e.javaClass.name, e.message.toString())
                false
            }

            if (remainingTimes <= 0 || !success)
                UpdateScheduler.closeCurrentRequest(context, appWidgetId)
            else
                UpdateScheduler.replaceCurrentRequest(context, appWidgetId, remainingTimes - 1, 30.seconds)
        }
    }
}