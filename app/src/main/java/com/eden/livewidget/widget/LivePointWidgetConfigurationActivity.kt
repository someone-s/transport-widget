package com.eden.livewidget.widget

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.eden.livewidget.R

class LivePointWidgetConfigurationActivity: ComponentActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        createNotificationChannel()

        createWidget(appWidgetId)
    }

    private fun createWidget(appWidgetId: Int) {

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()

        val inputData = Data.Builder()
            .putInt(LivePointWidgetCreateWorker.APP_WIDGET_ID, appWidgetId)
            .putString(LivePointWidgetCreateWorker.STOP_POINT_ID, "940GZZLUASL")
            .build()

        val workerRequest = OneTimeWorkRequestBuilder<LivePointWidgetCreateWorker>()
            .setInputData(inputData)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(this).enqueue(workerRequest)
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                LivePointWidgetCreateWorker.NOTIFICATION_CHANNEL_ID,
                "Widget Changes",
                NotificationManager.IMPORTANCE_LOW
            )
            NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
        }
    }
}

class LivePointWidgetCreateWorker(
    val context: Context,
    val params: WorkerParameters,
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
        setForeground(getForegroundInfo())

        val appWidgetId = inputData.getInt(APP_WIDGET_ID, -1)
        val stopPointId = inputData.getString(STOP_POINT_ID)
        if (stopPointId == null)
            return Result.failure()

        val manager = GlanceAppWidgetManager(context)
        // if illegal exception let worker fail
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.stopPointIdKey] = stopPointId
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