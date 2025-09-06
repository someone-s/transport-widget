package com.eden.livewidget.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.eden.livewidget.data.points.PointsRepository
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.providerFromString
import com.eden.livewidget.data.providerToString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DataSyncWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_PROVIDER = "provider"
        const val NOTIFICATION_ID = 50002

        private fun getUniqueWorkName(apiProvider: Provider) =
            "Data Sync Worker ${providerToString(apiProvider)}"

        fun getWorkInfoFlow(context: Context, provider: Provider): Flow<List<WorkInfo>> {

            return WorkManager.Companion.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(getUniqueWorkName(provider))
        }

        fun getIsActiveFlow(context: Context, provider: Provider): Flow<Boolean>  {
            return getWorkInfoFlow(context, provider)
                .map { workInfos ->
                    if (workInfos.isEmpty()) return@map false

                    for (info in workInfos) {
                        if (!info.state.isFinished)
                            return@map true
                    }
                    return@map false
                }
        }

        fun cancelCurrentRequest(context: Context, provider: Provider) {
            WorkManager.Companion.getInstance(context).cancelUniqueWork(
                getUniqueWorkName(provider)
            )
        }

        fun schedule(context: Context, provider: Provider): UUID {

            val inputData = Data.Builder()
                .putString(KEY_PROVIDER, providerToString(provider))
                .build()

            val builder = OneTimeWorkRequestBuilder<DataSyncWorker>()
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            val workerRequest = builder.build()

            WorkManager.Companion.getInstance(context).enqueueUniqueWork(
                getUniqueWorkName(provider),
                ExistingWorkPolicy.REPLACE,
                workerRequest
            )

            return workerRequest.id
        }

    }

    override suspend fun doWork(): Result {

        val provider = providerFromString(inputData.getString(KEY_PROVIDER))
            ?: return Result.failure()

        // Mark the Worker as important
        val progress = context.getString(R.string.data_sync_worker_begin_progress_text)
        setForeground(createForegroundInfo(progress))

        download(provider)

        return Result.success()
    }

    private suspend fun download(provider: Provider) {
        // Downloads a file and updates bytes read
        // Calls setForeground() periodically when it needs to update
        // the ongoing Notification
        val repository = PointsRepository.Companion.getInstance(context, provider)
        repository.refresh(context) { status ->
            setForegroundAsync(createForegroundInfo(status))
        }

    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = applicationContext.getString(R.string.data_sync_notification_channel_id)
        val title = applicationContext.getString(R.string.data_sync_notification_title)
        val cancel = applicationContext.getString(R.string.data_sync_notification_cancel)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.Companion.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_data_sync_icon)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()


        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        else
            ForegroundInfo(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        // Create a Notification channel
        val notificationChannel = NotificationChannel(
            applicationContext.getString(R.string.data_sync_notification_channel_id),
            applicationContext.getString(R.string.data_sync_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
    }

    // Needed for pre android 12
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(context.getString(R.string.data_sync_worker_begin_progress_text))
    }
}