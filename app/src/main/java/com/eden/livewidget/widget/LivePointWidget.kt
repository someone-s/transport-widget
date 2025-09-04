package com.eden.livewidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.eden.livewidget.MainActivity
import com.eden.livewidget.data.LivePointRepository

class LivePointWidget : GlanceAppWidget() {

    companion object {

        val stopPointIdKey = stringPreferencesKey("stopPointId")

    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            val stopPointId = currentState(stopPointIdKey)
            if (stopPointId != null) {
                MyContent(stopPointId)
            }
            // create your AppWidget here
        }
    }


    @Composable
    private fun MyContent(stopPointId: String) {
        val repository = remember { LivePointRepository.getInstance(stopPointId) }
        val latestArrivals by repository.latestArrivals.collectAsState()

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Where to?", modifier = GlanceModifier.padding(12.dp))
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(latestArrivals) { arrival ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = arrival.service,
                            maxLines = 1
                        )
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = arrival.service,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Button(
                text = "Refresh",
                onClick = actionRunCallback<RefreshLivePointWidgetCallback>()
            )
            Button(
                text = "Work",
                onClick = actionStartActivity<MainActivity>()
            )
        }
    }
}

class RefreshLivePointWidgetCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        // Glance use this instance to generate RemoteView to show
        val updater = LivePointWidget()

        GlanceAppWidgetManager(context)
        // PreferencesGlanceStateDefinition is the default state definition used
        val preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val stopPointId = preferences[LivePointWidget.stopPointIdKey]
        if (stopPointId == null)
            return

        val repository = LivePointRepository.getInstance(stopPointId)
        repository.fetchLatestArrival()

        updater.update(context, glanceId)
    }

}