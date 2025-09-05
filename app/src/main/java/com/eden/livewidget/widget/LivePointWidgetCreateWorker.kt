package com.eden.livewidget.widget

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.eden.livewidget.R

class LivePointWidgetCreateWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val APP_WIDGET_ID = "appWidgetId"
        const val STOP_POINT_ID = "stopPointId"
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
        val stopPointId = inputData.getString(STOP_POINT_ID)
        if (stopPointId == null)
            return Result.failure()

        val manager = GlanceAppWidgetManager(context)
        // if illegal exception let worker fail
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.STOP_POINT_KEY] = stopPointId
        }

        LivePointWidget().update(context, glanceId)

        return Result.success()
    }

    private fun createNotification() : Notification {

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Configuring Widget")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

