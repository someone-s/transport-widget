package com.eden.livewidget.widget

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.arrivals.ArrivalsRepository
import com.eden.livewidget.main.MainActivity
import java.util.Calendar

class LivePointWidget : GlanceAppWidget() {

    companion object {

        val API_PROVIDER_KEY = stringPreferencesKey("apiProvider")
        val API_VALUE_KEY = stringPreferencesKey("apiValue")
        val DISPLAY_NAME_KEY = stringPreferencesKey("displayName")

    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            GlanceTheme {
                val apiProviderString = currentState(API_PROVIDER_KEY)
                if (apiProviderString == null) {
                    PlaceholderContent()
                    return@GlanceTheme
                }

                var apiProvider: Provider
                try {
                    apiProvider = Provider.valueOf(apiProviderString)
                } catch (_: IllegalArgumentException) {
                    PlaceholderContent()
                    return@GlanceTheme
                }

                val apiValue = currentState(API_VALUE_KEY)
                if (apiValue == null) {
                    PlaceholderContent()
                    return@GlanceTheme
                }

                val displayName = currentState(DISPLAY_NAME_KEY)
                if (displayName == null) {
                    PlaceholderContent()
                    return@GlanceTheme
                }

                MyContent(context, id, apiProvider, apiValue, displayName)
            }
        }
    }

    @Composable
    private fun PlaceholderContent() {

        Scaffold(
            backgroundColor = GlanceTheme.colors.widgetBackground,
            modifier = GlanceModifier
                .fillMaxSize(),

            ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(12.dp)
                    .padding(all = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.widget_configure_prompt_text),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }
        }

    }


    @Composable
    private fun MyContent(
        context: Context,
        glanceId: GlanceId,
        apiProvider: Provider,
        apiValue: String,
        displayName: String,
    ) {

        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val flow = LivePointWidgetUpdateWorker.getIsActiveFlow(context, widgetId)
        val isActive by flow.collectAsState(false)

        Scaffold(
            backgroundColor = GlanceTheme.colors.widgetBackground,
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(
                    onClick = actionStartActivity<MainActivity>()
                )
            ,
            horizontalPadding = 16.dp,
            titleBar = {
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        ),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = GlanceModifier
                            .defaultWeight(),
                        text = displayName,
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 25.sp,
                        ),

                        maxLines = 1
                    )

                    if (isActive)
                        FilledButton(
                            icon = ImageProvider(R.drawable.ic_widget_refresh),
                            text = DateFormat.getTimeFormat(context).format(Calendar.getInstance().time),
                            onClick = actionRunCallback<RefreshLivePointWidgetCallback>(),
                        )
                }
            }
        ) {
            if (isActive)
                ActiveList(apiProvider, apiValue)
            else
                DisableBlock()
        }

    }

    @Composable
    fun ActiveList(apiProvider: Provider, apiValue: String) {
        val repository = remember { ArrivalsRepository.getInstance(apiProvider, apiValue) }
        val latestArrivals by repository.latestArrivals.collectAsState()

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            itemsIndexed(latestArrivals) { index, arrival ->
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                )
                {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(GlanceTheme.colors.primaryContainer)
                            .cornerRadius(12.dp)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = arrival.service,
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 25.sp,
                            ),
                            maxLines = 1
                        )
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text =
                                    if (arrival.remainingS < 60)
                                        LocalContext.current.getString(R.string.widget_arrival_imminent_text)
                                    else
                                        LocalContext.current.getString(
                                            R.string.widget_arrival_minute_text,
                                            (arrival.remainingS / 60)
                                        ),
                                style = TextStyle(
                                    color = GlanceTheme.colors.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 25.sp,
                                ),
                                maxLines = 1
                            )
                        }
                    }
                    if (index < latestArrivals.size - 1)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                }

            }
            item {
                Spacer(modifier = GlanceModifier.height(16.dp))
            }
        }


    }

    @Composable
    fun DisableBlock() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.primaryContainer)
                    .cornerRadius(12.dp)
                    .padding(all = 8.dp)
                    .clickable(
                        onClick = actionRunCallback<RefreshLivePointWidgetCallback>()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.widget_start_tracking_prompt_text),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

class RefreshLivePointWidgetCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {


        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        LivePointWidgetUpdateWorker.schedule(context, widgetId, 10, null)
    }
}
