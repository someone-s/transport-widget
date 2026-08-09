package com.eden.livewidget.main

import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.intSetOf
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.eden.livewidget.Agency
import com.eden.livewidget.main.ui.MainContent
import com.eden.livewidget.main.ui.notification.PermissionLogic
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.util.getSerializableExtraCompat
import com.eden.livewidget.widget.LivePointWidgetReceiver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val AGENCY_EXTRA_NAME = "agency"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val agency: Agency? = intent.getSerializableExtraCompat(AGENCY_EXTRA_NAME)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val context = this
            lifecycleScope.launch {
                val manager = GlanceAppWidgetManager(context)
                manager.setWidgetPreviews(
                    receiver = LivePointWidgetReceiver::class,
                    widgetCategories = intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            TransportWidgetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(this.application, this, agency)

                    PermissionLogic()
                }
            }
        }
    }
}