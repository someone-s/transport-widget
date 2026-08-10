package com.eden.livewidget.widget.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eden.livewidget.R
import java.util.concurrent.atomic.AtomicInteger

private const val NOTIFICATION_CHANNEL_ID = "Widget Update"

@RequiresApi(Build.VERSION_CODES.O)
private fun createChannel(context: Context) {

    Log.i(context.packageName, "Try to create notification channel")

    val notificationChannel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.widget_update_notification_title),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = context.getString(R.string.widget_update_notification_description)
    }
    NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
}

private val atomicInteger = AtomicInteger()

fun createScheduledNotification(context: Context, appWidgetId: Int): Int {

    val intent = Intent(context.applicationContext, UpdateCancelReceiver::class.java)
        .apply {
            putExtra(UpdateScheduler.APP_WIDGET_ID, appWidgetId)
        }

    val pendingIntent = intent
        .let { intent ->
            PendingIntent.getBroadcast(context.applicationContext, appWidgetId, intent, PendingIntent.FLAG_MUTABLE)
        }

    return createNotification(
        context,
        title = context.getString(R.string.widget_update_notification_title),
        deleteIntent = pendingIntent,
        deleteText = context.getString(R.string.widget_update_notification_cancel_text)
    )
}

fun createNotification(
    context: Context,
    title: String,
    text: String? = null,
    deleteIntent: PendingIntent? = null,
    deleteText: String? = null
): Int {

    // Create a Notification channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createChannel(context)
    }

    Log.i(context.packageName, "Try to create notification")

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .apply {
            setSmallIcon(R.drawable.ic_logo_full)
            setContentTitle(title)
            if (text != null)
                setContentText(text)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            if (deleteIntent != null) {
                setDeleteIntent(deleteIntent)
                if (deleteText != null)
                    addAction(0, deleteText, deleteIntent)
            }
            setSilent(true)
        }
        .build()

    val notificationId = atomicInteger.incrementAndGet()

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(context.packageName, "No permission to notify notification")
            return@with
        }

        Log.i(context.packageName, "Try to notify notification")
        notify(notificationId, notification)
    }

    return notificationId
}

fun cancelNotification(context: Context, notificationId: Int) {
    Log.i(context.packageName, "Try to cancel notification")

    with(NotificationManagerCompat.from(context)) {
        cancel(notificationId)
    }
}