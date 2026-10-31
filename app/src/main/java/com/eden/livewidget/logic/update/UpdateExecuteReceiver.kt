package com.eden.livewidget.logic.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.eden.livewidget.LiveWidgetApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class UpdateExecuteReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val appContainer = (context.applicationContext as LiveWidgetApplication).container
        val appWidgetId = intent.getIntExtra(UpdateScheduler.APP_WIDGET_ID, -1)

        val remainingTimes = intent.getIntExtra(UpdateScheduler.REMAINING_TIMES, -1)

        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val updateSuccess = try {
                    updateWidget(context, appContainer, appWidgetId, remainingTimes)
                } catch (e: Exception) {
                    Log.e(e.javaClass.name, e.message.toString())
                    false
                }

                if (remainingTimes <= 0 || !updateSuccess)
                    UpdateScheduler.closeCurrentRequest(context, appWidgetId)
                else
                    UpdateScheduler.replaceCurrentRequest(
                        context,
                        appWidgetId,
                        remainingTimes - 1,
                        30.seconds
                    )
            }
            finally {
                pendingResult.finish()
            }
        }
    }
}