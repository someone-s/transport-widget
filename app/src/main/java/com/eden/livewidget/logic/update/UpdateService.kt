package com.eden.livewidget.logic.update

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.collection.MutableIntSet
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.eden.livewidget.BuildConfig
import com.eden.livewidget.LiveWidgetApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class UpdateService: LifecycleService() {

    companion object {
        /**
         * Call with [android.content.Context.startForegroundService];
         * Requires [EXTRA_APP_WIDGET_ID]
         */
        const val ACTION_UPDATE = "${BuildConfig.APPLICATION_ID}.ACTION_UPDATE"
        /**
         * Call with [android.content.Context.startService];
         * Requires [EXTRA_APP_WIDGET_ID]
         */
        const val ACTION_PAUSE = "${BuildConfig.APPLICATION_ID}.ACTION_PAUSE"
        const val EXTRA_APP_WIDGET_ID = "${BuildConfig.APPLICATION_ID}.EXTRA_APP_WIDGET_ID"

        private val FOREGROUND_SERVICE_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        else 0
    }

    private var updateLogic: UpdateLogic? = null
    private var updateNotification: UpdateNotification? = null

    private val activeJobs = mutableMapOf<Int, Job>()

    override fun onCreate() {
        super.onCreate()
        updateLogic = UpdateLogic(
            context = this,
            appContainer = (application as LiveWidgetApplication).container,
            widgetRepository = (application as LiveWidgetApplication).container.widgetRepository,
        )
        updateNotification = UpdateNotification(
            context = this,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Intent can be null from recreated service
        if (intent != null)
            try {
                when(intent.action) {
                    ACTION_UPDATE -> onUpdate(intent, startId)
                    ACTION_PAUSE -> onPause(intent, startId)
                }
            } catch (e: Exception) {
                Log.e("$javaClass", e.message, e)
                stopSelf(startId)
            }

        // Restart if killed after this return
        return START_NOT_STICKY
    }


    private fun onUpdate(intent: Intent, startId: Int) = synchronized(this) {
        val capturedLogic = updateLogic
        checkNotNull(capturedLogic) { "Logic not initialized." }
        val capturedNotification = updateNotification
        checkNotNull(capturedNotification) { "Logic not initialized." }

        val appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        assert(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) { "Invalid app widget id." }

        val notification = capturedNotification.getNotification(appWidgetId)
        ServiceCompat.startForeground(
            this,
            0,
            notification,
            FOREGROUND_SERVICE_TYPE
        )

        val existingJob = activeJobs[appWidgetId]

        val newJob = lifecycleScope
            .launch {
                existingJob?.cancelAndJoin()

                capturedLogic.update(appWidgetId)
                capturedLogic.end(appWidgetId)

                // If new active added between check and use,
                // stopSelf(startId) will ignore as newer startId must exist
                if (!capturedLogic.isAnyActive())
                    stopSelf(startId)
            }
        activeJobs[appWidgetId] = newJob
        newJob.invokeOnCompletion { activeJobs.remove(appWidgetId) }
    }

    private fun onPause(intent: Intent, startId: Int) = synchronized(this) {
        val capturedLogic = updateLogic
        checkNotNull(capturedLogic) { "Logic not initialized." }

        val appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        assert(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) { "Invalid app widget id." }

        val existingJob = activeJobs[appWidgetId]

        lifecycleScope
            .launch {
                existingJob?.cancelAndJoin()

                capturedLogic.end(appWidgetId)

                // If new active added between check and use,
                // stopSelf(startId) will ignore as newer startId must exist
                if (!capturedLogic.isAnyActive())
                    stopSelf(startId)

            }
            .invokeOnCompletion { activeJobs.remove(appWidgetId) }

    }
}