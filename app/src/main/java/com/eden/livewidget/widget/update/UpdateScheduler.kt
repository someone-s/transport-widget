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

                if (activeWidgetIds.isEmpty()) {
                    Log.i(this.javaClass.name, "No active flow for any widgets")
                    return@map false
                }

                if (!activeWidgetIds.containsKey(appWidgetId)) {
                    Log.i(this.javaClass.name, "No active flow for widget $appWidgetId")
                    return@map false
                }

                return@map true
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
            val newAlarmState = construct(context, appWidgetId, remainingTimes).apply {

                activeAlarms.getAndUpdate { previousMap ->
                    if (this != null)
                        previousMap + (appWidgetId to this)
                    else
                        previousMap - appWidgetId
                }

                if (previousNotificationId != null)
                    cancelNotification(context, previousNotificationId)
            }

            if (newAlarmState != null)
                push(context, newAlarmState, delay).apply {

                    if (!this) {
                        activeAlarms.getAndUpdate { previousMap ->
                            previousMap - appWidgetId
                        }

                        cancelNotification(context, newAlarmState.notificationId)
                    }
                }

        }

        private fun construct(
            context: Context,
            appWidgetId: Int,
            remainingTimes: Int,
        ): AlarmState? {


            Log.i(this.javaClass.name, "Trying to construct for widget $appWidgetId")
            Log.i(this.javaClass.name, "Remaining times: $remainingTimes")

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
                    PendingIntent.getBroadcast(
                        context.applicationContext,
                        appWidgetId, intent,
                        PendingIntent.FLAG_MUTABLE or
                                PendingIntent.FLAG_ONE_SHOT
                    )
                }

            return AlarmState(pendingIntent, notificationId)
        }

        private fun push(
            context: Context,
            alarmState: AlarmState,
            delay: Duration?,
        ): Boolean {

            Log.i(this.javaClass.name, "Trying to schedule update for widget")

            fun pushDirect(): Boolean {
                alarmState.pendingIntent.send()
                return true
            }

            fun pushSchedule(delayNotNull: Duration): Boolean {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager == null) {
                    Log.e(this.javaClass.name, "AlarmManager is null")
                    return false
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + delayNotNull.inWholeMilliseconds,
                            alarmState.pendingIntent
                        )
                    }
                    else {
                        alarmManager.set(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + delayNotNull.inWholeMilliseconds,
                            alarmState.pendingIntent
                        )
                    }
                } else {
                    alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + delayNotNull.inWholeMilliseconds,
                        alarmState.pendingIntent
                    )
                }

                Log.i(this.javaClass.name, "Scheduled alarm for widget")

                return true
            }

            if (delay == null || delay == Duration.ZERO) {
                Log.i(this.javaClass.name, "Scheduled for immediate, directly send intent instead")
                return pushDirect()
            }
            else {
                Log.i(this.javaClass.name, "Scheduled for immediate, directly send intent instead")
                return pushSchedule(delay)
            }
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