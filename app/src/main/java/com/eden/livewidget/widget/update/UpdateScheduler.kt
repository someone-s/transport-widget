package com.eden.livewidget.widget.update

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

class UpdateScheduler {

    data class AlarmState(
        val pendingIntent: PendingIntent,
        val notificationId: Int,
    )
    companion object {
        const val APP_WIDGET_ID = "appWidgetId"
        const val REMAINING_TIMES = "remainingTimes"

        private val activeAlarms = MutableStateFlow(mapOf<Int, AlarmState>())

        fun getIsActiveFlow(appWidgetId: Int): Flow<Boolean> {
            return activeAlarms.map { activeWidgetIds ->

                if (activeWidgetIds.isEmpty())
                    return@map false

                if (activeWidgetIds.containsKey(appWidgetId))
                    return@map true

                return@map false
            }
        }

        fun setCurrentRequest(context: Context, appWidgetId: Int, remainingTimes: Int, delay: Duration?) {
            val alarmState = schedule(context, appWidgetId, remainingTimes, delay) ?: return

            activeAlarms.getAndUpdate { previousMap ->
                previousMap + (appWidgetId to alarmState)
            }
        }

        fun cancelCurrentRequest(context: Context, appWidgetId: Int) {

            cancel(context, appWidgetId)

            activeAlarms.getAndUpdate { previousMap ->
                previousMap - appWidgetId
            }
        }

        fun closeCurrentRequest(context: Context, appWidgetId: Int) {

            val alarmState = activeAlarms.value[appWidgetId]
            val notificationId = alarmState?.notificationId

            activeAlarms.getAndUpdate { previousMap ->
                previousMap - appWidgetId
            }

            if (notificationId != null)
                cancelNotification(context, notificationId)
        }

        // seamless close and set new
        fun replaceCurrentRequest(context: Context, appWidgetId: Int, remainingTimes: Int, delay: Duration?) {

            val previousAlarmState = activeAlarms.value[appWidgetId]
            val previousNotificationId = previousAlarmState?.notificationId

            // new notification created here
            val newAlarmState = schedule(context, appWidgetId, remainingTimes, delay)

            activeAlarms.getAndUpdate { previousMap ->
                if (newAlarmState != null)
                    previousMap + (appWidgetId to newAlarmState)
                else
                    previousMap - appWidgetId
            }

            if (previousNotificationId != null)
                cancelNotification(context, previousNotificationId)
        }

        private fun schedule(context: Context, appWidgetId: Int, remainingTimes: Int, delay: Duration?): AlarmState? {

            Log.i(this.javaClass.name, "Trying to schedule for widget $appWidgetId")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e(this.javaClass.name, "AlarmManager is null")
                return null
            }

            if (remainingTimes < 0) {
                Log.e(this.javaClass.name, "Remaining times < 0 widget $appWidgetId")
                return null
            }

            cancel(context, appWidgetId)

            val notificationId = createScheduledNotification(context, appWidgetId)

            val intent = Intent(context.applicationContext, UpdateExecuteReceiver::class.java)
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

            return AlarmState(pendingIntent, notificationId)
        }

        private fun cancel(context: Context, appWidgetId: Int) {

            Log.i(this.javaClass.name, "Trying to cancel for widget $appWidgetId")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e(this.javaClass.name, "AlarmManager is null")
                return
            }

            val alarmState = activeAlarms.value[appWidgetId]
            if (alarmState == null) {
                Log.i(this.javaClass.name, "No pending alarm to cancel for widget with id $appWidgetId")
                return
            }

            alarmManager.cancel(alarmState.pendingIntent)
            cancelNotification(context, alarmState.notificationId)

            Log.i(this.javaClass.name, "Cancelled alarm for widget $appWidgetId")
        }
    }

}