package com.eden.livewidget.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.eden.livewidget.data.utils.Provider
import com.eden.livewidget.data.utils.providerToString
import com.eden.livewidget.ui.configuration.ConfiguratorContent
import com.eden.livewidget.ui.theme.TransportWidgetsTheme

class LivePointWidgetConfigurationActivity: ComponentActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        createNotificationChannel()

        enableEdgeToEdge()
        setContent {
            TransportWidgetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfiguratorContent({ apiProvider, apiValue, displayName -> createWidget(appWidgetId, apiProvider, apiValue, displayName) })
                }
            }
        }
    }



    private fun createWidget(appWidgetId: Int, apiProvider: Provider, apiValue: String, displayName: String) {

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()

        val inputData = Data.Builder()
            .putInt(LivePointWidgetCreateWorker.APP_WIDGET_ID, appWidgetId)
            .putString(LivePointWidgetCreateWorker.API_PROVIDER, providerToString(apiProvider))
            .putString(LivePointWidgetCreateWorker.API_VALUE, apiValue) // "490001015BJ"
            .putString(LivePointWidgetCreateWorker.DISPLAY_NAME, displayName) // "490001015BJ"
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                LivePointWidgetUpdateWorker.NOTIFICATION_CHANNEL_ID,
                "Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            )
            NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
        }
    }
}
