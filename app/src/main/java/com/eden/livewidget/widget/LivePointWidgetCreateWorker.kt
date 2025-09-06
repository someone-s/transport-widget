package com.eden.livewidget.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.eden.livewidget.R
import com.eden.livewidget.data.providerFromString
import com.eden.livewidget.data.providerToString

class LivePointWidgetCreateWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val APP_WIDGET_ID = "appWidgetId"
        const val API_PROVIDER = "apiProvider"
        const val API_VALUE = "apiValue"
        const val DISPLAY_NAME = "displayName"
        const val NOTIFICATION_ID = 50000
        const val NOTIFICATION_CHANNEL_ID = "Widget Worker"


    }

    // Needed for pre android 12
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID, createNotification()
        )
    }

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
            setForeground(getForegroundInfo())

        val appWidgetId = inputData.getInt(APP_WIDGET_ID, -1)

        val manager = GlanceAppWidgetManager(context)
        var glanceId: GlanceId
        try {
            glanceId = manager.getGlanceIdBy(appWidgetId)
        } catch (_: IllegalArgumentException) {
            return Result.failure()
        }

        val apiProvider = providerFromString(inputData.getString(API_PROVIDER))
        if (apiProvider == null)
            return Result.failure()

        val apiValue = inputData.getString(API_VALUE)
        if (apiValue == null)
            return Result.failure()

        val displayName = inputData.getString(DISPLAY_NAME)
        if (displayName == null)
            return Result.failure()

        // Stop any ongoing update
        LivePointWidgetUpdateWorker.Companion.unsetCurrentRequestId(appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.API_PROVIDER_KEY] = providerToString(apiProvider)
            preferences[LivePointWidget.API_VALUE_KEY] = apiValue
            preferences[LivePointWidget.DISPLAY_NAME_KEY] = displayName
            preferences[LivePointWidget.IS_ACTIVE_KEY] = LivePointWidget.IS_ACTIVE_FALSE
        }

        LivePointWidget().update(context, glanceId)

        return Result.success()
    }

    private fun createNotification() : Notification {

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(context.getString(R.string.widget_create_worker_notification_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.widget_create_worker_notification_title),
            NotificationManager.IMPORTANCE_LOW
        )
        NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
    }
}

