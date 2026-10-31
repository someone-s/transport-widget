package com.eden.livewidget.ui.main

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
import com.eden.livewidget.LiveWidgetApplication
import com.eden.livewidget.data.Agency
import com.eden.livewidget.ui.main.notification.PermissionLogic
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.ui.widget.LivePointWidgetReceiver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val AGENCY_EXTRA_NAME = "agency"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as LiveWidgetApplication).container
        val agencies: List<Agency> = appContainer.agencies
        val targetAgency: Agency? = intent.getStringExtra(AGENCY_EXTRA_NAME)?.let { appContainer.agencyFromId(it) }

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
                    MainContent(agencies, targetAgency)

                    PermissionLogic()
                }
            }
        }
    }
}