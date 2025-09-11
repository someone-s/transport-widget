package com.eden.livewidget.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.eden.livewidget.R
import com.eden.livewidget.data.arrivals.ArrivalsRepository
import com.eden.livewidget.data.providerFromString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class LivePointWidgetUpdateWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val APP_WIDGET_ID = "appWidgetId"
        const val REMAINING_TIMES = "remainingTimes"
        const val NOTIFICATION_ID = 50001
        const val NOTIFICATION_CHANNEL_ID = "Widget Worker"


        private fun getUniqueWorkName(appWidgetId: Int) =
            "Widget Update Worker $appWidgetId"

        fun getWorkInfoFlow(context: Context, appWidgetId: Int): Flow<List<WorkInfo>> {
            return WorkManager.Companion.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(getUniqueWorkName(appWidgetId))
        }

        fun getIsActiveFlow(context: Context, appWidgetId: Int): Flow<Boolean> {
            return getWorkInfoFlow(context, appWidgetId).map { workInfos ->
                if (workInfos.isEmpty()) return@map false

                for (info in workInfos) {
                    if (!info.state.isFinished)
                        return@map true
                }
                return@map false
            }
        }


        fun cancelCurrentRequest(context: Context, appWidgetId: Int) {
            WorkManager.Companion.getInstance(context).cancelUniqueWork(
                getUniqueWorkName(appWidgetId)
            )
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
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    builder.setInitialDelay(delay.toJavaDuration())
                else
                    builder.setInitialDelay(delay.inWholeNanoseconds, TimeUnit.NANOSECONDS)
            }
            val workerRequest = builder.build()

            WorkManager.Companion.getInstance(context).enqueueUniqueWork(
                getUniqueWorkName(appWidgetId),
                ExistingWorkPolicy.REPLACE,
                workerRequest
            )
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


        val manager = GlanceAppWidgetManager(context)
        // if illegal exception let worker fail
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        // Glance use this instance to generate RemoteView to show
        val updater = LivePointWidget()


        val remainingTimes = inputData.getInt(REMAINING_TIMES, -1)

        if (remainingTimes < 0) {

            updateAppWidgetState(context, glanceId) { preferences ->
                preferences[LivePointWidget.INACTIVE_TEXT_OPTION_KEY] =
                    LivePointWidget.INACTIVE_TEXT_OPTION_NORMAL
            }

            // update one more cycle then end
            updater.update(context, glanceId)
        } else {

            // PreferencesGlanceStateDefinition is the default state definition used
            val preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

            val apiProvider = providerFromString(preferences[LivePointWidget.API_PROVIDER_KEY])
            if (apiProvider == null)
                return Result.failure()

            val apiValue = preferences[LivePointWidget.API_VALUE_KEY]
            if (apiValue == null)
                return Result.failure()

            try {
                // Update data source
                val repository = ArrivalsRepository.getInstance(apiProvider, apiValue)
                repository.fetchLatestArrival()
            } catch (e: Exception) {
                Log.e(javaClass.name, e.message ?: "Failed with no message", e)

                val powerService = context.getSystemService(POWER_SERVICE)
                if (powerService == null)
                    updateAppWidgetState(context, glanceId) { preferences ->
                        preferences[LivePointWidget.INACTIVE_TEXT_OPTION_KEY] =
                            LivePointWidget.INACTIVE_TEXT_OPTION_ERROR
                    }
                else {
                    val powerManager = powerService as PowerManager
                    if (powerManager.isIgnoringBatteryOptimizations(context.packageName))
                        updateAppWidgetState(context, glanceId) { preferences ->
                            preferences[LivePointWidget.INACTIVE_TEXT_OPTION_KEY] =
                                LivePointWidget.INACTIVE_TEXT_OPTION_ERROR
                        }
                    else
                        updateAppWidgetState(context, glanceId) { preferences ->
                            preferences[LivePointWidget.INACTIVE_TEXT_OPTION_KEY] =
                                LivePointWidget.INACTIVE_TEXT_OPTION_BATTERY
                        }
                }
                updater.update(context, glanceId)

                return Result.failure()
            }

            updateAppWidgetState(context, glanceId) { preferences ->
                preferences[LivePointWidget.INACTIVE_TEXT_OPTION_KEY] =
                    LivePointWidget.INACTIVE_TEXT_OPTION_NORMAL
            }

            updater.update(context, glanceId)

            schedule(context, appWidgetId, remainingTimes - 1, 30.seconds)

        }

        return Result.success()
    }

    private fun createNotification(): Notification {

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