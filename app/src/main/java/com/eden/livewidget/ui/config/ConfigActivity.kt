package com.eden.livewidget.ui.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.ui.widget.LivePointWidget
import com.eden.livewidget.logic.update.UpdateService
import kotlinx.coroutines.launch

class ConfigActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            return

        val manager = GlanceAppWidgetManager(this)
        val glanceId = try {
            manager.getGlanceIdBy(appWidgetId)
        } catch (_: IllegalArgumentException) {
            finish()
            return
        }

        val context = this

        enableEdgeToEdge()
        setContent {
            TransportWidgetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    val coroutineScope = rememberCoroutineScope()

                    ConfigScreen(
                        appWidgetId = appWidgetId,
                        onExit = {
                            coroutineScope.launch {
                                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                setResult(RESULT_OK, resultValue)

                                // Stop any ongoing update
                                startService(Intent(context, UpdateService::class.java).apply {
                                    action = UpdateService.ACTION_PAUSE
                                    putExtra(UpdateService.EXTRA_APP_WIDGET_ID, appWidgetId)
                                })

                                LivePointWidget().update(context, glanceId)

                            }.invokeOnCompletion { finish() }
                        },
                    )


                }

            }
        }
    }
}