package com.eden.livewidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.eden.livewidget.util.goAsync
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

class UpdateScheduler : BroadcastReceiver() {

    companion object {
        const val APP_WIDGET_ID = "appWidgetId"
        const val REMAINING_TIMES = "remainingTimes"

        private val activeAlarms = MutableStateFlow(mapOf<Int, PendingIntent>())

        fun getIsActiveFlow(appWidgetId: Int): Flow<Boolean> {
            return activeAlarms.map { activeWidgetIds ->

                if (activeWidgetIds.isEmpty())
                    return@map false

                if (activeWidgetIds.containsKey(appWidgetId))
                    return@map true

                return@map false
            }
        }

        fun cancelCurrentRequest(context: Context, appWidgetId: Int) {

            cancelCurrentRequestNoUpdate(context, appWidgetId)

            activeAlarms.getAndUpdate { previousMap ->
                previousMap - appWidgetId
            }
        }

        private fun cancelCurrentRequestNoUpdate(context: Context, appWidgetId: Int) {

            Log.i(this.javaClass.name, "Trying to cancel for widget $appWidgetId")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e(this.javaClass.name, "AlarmManager is null")
                return
            }

            val pendingIntent = activeAlarms.value[appWidgetId]
            if (pendingIntent == null) {
                Log.i(this.javaClass.name, "No pending alarm to cancel for widget with id $appWidgetId")
                return
            }

            alarmManager.cancel(pendingIntent)

            Log.i(this.javaClass.name, "Cancelled alarm for widget $appWidgetId")

        }

        fun schedule(context: Context, appWidgetId: Int, remainingTimes: Int, delay: Duration?) {

            Log.i(this.javaClass.name, "Trying to schedule for widget $appWidgetId")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e(this.javaClass.name, "AlarmManager is null")
                return
            }

            cancelCurrentRequestNoUpdate(context, appWidgetId)

            val intent = Intent(context.applicationContext, UpdateScheduler::class.java)
                .apply {
                    putExtra(APP_WIDGET_ID, appWidgetId)
                    putExtra(REMAINING_TIMES, remainingTimes)
                }

            val pendingIntent = intent
                .let { intent ->
                    PendingIntent.getBroadcast(context.applicationContext, appWidgetId, intent, PendingIntent.FLAG_MUTABLE)
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + (delay?.inWholeMilliseconds ?: 0L),
                        pendingIntent
                    )
                }
                else {
                    alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + (delay?.inWholeMilliseconds ?: 0L),
                        pendingIntent
                    )
                }
            } else {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + (delay?.inWholeMilliseconds ?: 0L),
                    pendingIntent
                )
            }

            Log.i(this.javaClass.name, "Scheduled alarm for widget $appWidgetId")

            activeAlarms.getAndUpdate { previousMap ->
                previousMap + (appWidgetId to pendingIntent)
            }
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) {
            Log.e(this.javaClass.name, "Context is null")
            return
        }

        val appWidgetId = intent?.getIntExtra(APP_WIDGET_ID, -1) ?: -1

        activeAlarms.getAndUpdate { previousMap ->
            previousMap - appWidgetId
        }

        val remainingTimes = intent?.getIntExtra(REMAINING_TIMES, -1) ?: -1

        @OptIn(DelicateCoroutinesApi::class)
        goAsync(GlobalScope, Dispatchers.Default) {
            updateWidget(context, appWidgetId, remainingTimes)
        }
    }

}