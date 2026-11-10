package com.eden.livewidget.ui.widget.pause.error

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.ui.util.getExplicitKeySettingsIntent
import com.eden.livewidget.ui.util.getResolvedOpenUriIntent

@Composable
fun AuthenticateErrorBlock(
    appWidgetId: Int,
    agency: Agency?
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            RetrySurface(
                appWidgetId = appWidgetId,
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.widget_retry_authenticate_reason_text),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        }
        Box(
            modifier = GlanceModifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                UpdateKeyGroup(agency)
            }
        }
    }
}

@Composable
private fun UpdateKeyGroup(agency: Agency?) {

    val openUrlAction = remember(agency) {
        val (intent, bundle) =
            if (agency != null)
                getResolvedOpenUriIntent(agency.agencyHelp)
            else Pair(Intent(), null)
        @OptIn(ExperimentalGlanceApi::class)
        actionStartActivity(
            intent = intent,
            activityOptions = bundle
        )
    }

    val explicitKeySettingsIntent =
        if (agency != null)
            getExplicitKeySettingsIntent(LocalContext.current, agency)
        else Intent()

    Row(
        modifier = GlanceModifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledButton(
            icon = ImageProvider(R.drawable.corporate_fare_filled),
            text = LocalContext.current.getString(R.string.widget_retry_authenticate_go_to_service_text),
            onClick = openUrlAction,
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2
        )
        Image(
            provider = ImageProvider(R.drawable.ic_shared_outlined_arrow_right_alt),
            contentDescription = LocalContext.current.getString(R.string.widget_retry_option_icon),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_outlined_key),
            text = LocalContext.current.getString(R.string.widget_retry_authenticate_update_key_text),
            onClick = actionStartActivity(explicitKeySettingsIntent),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2
        )
    }
}