package com.eden.livewidget.logic.update

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import com.eden.livewidget.R

class UpdateNotification(
    private val context: Context,
) {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "Widget Update"
    }

    private val channel by lazy {
        Log.i(context.packageName, "Try to create notification channel")

        NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .apply {
                setName(context.getString(R.string.widget_update_notification_title))
                setDescription(context.getString(R.string.widget_update_notification_description))
            }
            .build()
            .also {
                NotificationManagerCompat.from(context).createNotificationChannel(it)
            }
    }

    fun getNotification(requestCode: Int): Notification {

        val pauseIntent = PendingIntentCompat.getService(
            context,
            requestCode,
            Intent(context, UpdateService::class.java).apply {
                action = UpdateService.ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT,
            false
        )

        Log.i(context.packageName, "Try to create notification")

        return NotificationCompat.Builder(context, channel.id)
            .apply {
                setPriority(NotificationCompat.PRIORITY_HIGH)

                setSmallIcon(R.drawable.ic_logo_full)
                setContentTitle(context.getString(R.string.widget_update_notification_title))

                setDeleteIntent(pauseIntent)
                addAction(
                    R.drawable.ic_shared_outlined_pause,
                    context.getString(R.string.widget_update_notification_cancel_text),
                    pauseIntent
                )

                setSilent(true)
            }
            .build()
    }
}
