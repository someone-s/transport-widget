package com.eden.livewidget.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.eden.livewidget.R
import com.eden.livewidget.data.arrivals.ArrivalsRepository
import com.eden.livewidget.data.utils.providerFromString
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class LivePointWidgetUpdateWorker(
    val context: Context,
    val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val APP_WIDGET_ID = "appWidgetId"
        const val REMAINING_TIMES = "remainingTimes"
        const val NOTIFICATION_ID = 50001
        const val NOTIFICATION_CHANNEL_ID = "Widget Worker"

        private val mutex = Mutex()

        private val currentRequestIds = mutableMapOf<Int, UUID>()

        fun unsetCurrentRequestId( appWidgetId: Int) {
            currentRequestIds.remove(appWidgetId)
        }

        fun schedule(context: Context, appWidgetId: Int, remainingTimes: Int, delay: Duration?) {
            val inputData = Data.Builder()
                .putInt(APP_WIDGET_ID, appWidgetId)
                .putInt(REMAINING_TIMES, remainingTimes)
                .build()

            val builder = OneTimeWorkRequestBuilder<LivePointWidgetUpdateWorker>()
                .setInputData(inputData)
            if (delay == null)
                builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            else
                builder.setInitialDelay(delay.toJavaDuration())
            val workerRequest = builder.build()

            currentRequestIds[appWidgetId] = workerRequest.id

            WorkManager.getInstance(context).enqueue(workerRequest)
        }

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

        mutex.withLock {
            // don't schedule new job if not main
            if (currentRequestIds[appWidgetId] != params.id)
                return Result.success()
        }

        val manager = GlanceAppWidgetManager(context)
        // if illegal exception let worker fail
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        // Glance use this instance to generate RemoteView to show
        val updater = LivePointWidget()


        val remainingTimes = inputData.getInt(REMAINING_TIMES, -1)

        if (remainingTimes < 0) {

            updateAppWidgetState(context, glanceId) { preferences ->
                preferences[LivePointWidget.IS_ACTIVE_KEY] = LivePointWidget.IS_ACTIVE_FALSE
            }
            updater.update(context, glanceId)
        }
        else {

            updateAppWidgetState(context, glanceId) { preferences ->
                preferences[LivePointWidget.IS_ACTIVE_KEY] = LivePointWidget.IS_ACTIVE_TRUE
            }

            // PreferencesGlanceStateDefinition is the default state definition used
            val preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

            val apiProvider = providerFromString(preferences[LivePointWidget.API_PROVIDER_KEY])
            if (apiProvider == null)
                return Result.failure()

            val apiValue = preferences[LivePointWidget.API_VALUE_KEY]
            if (apiValue == null)
                return Result.failure()

            // Update data source
            val repository = ArrivalsRepository.getInstance(apiProvider, apiValue)
            repository.fetchLatestArrival()

            updater.update(context, glanceId)

            schedule(context, appWidgetId, remainingTimes - 1, 10.seconds)

        }

        return Result.success()
    }

    private fun createNotification() : Notification {

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(context.getString(R.string.widget_update_worker_notification_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.widget_update_worker_notification_title),
            NotificationManager.IMPORTANCE_LOW
        )
        NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
    }
}