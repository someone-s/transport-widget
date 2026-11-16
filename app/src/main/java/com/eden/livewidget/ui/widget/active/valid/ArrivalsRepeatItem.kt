package com.eden.livewidget.ui.widget.active.valid

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.data.agency.common.arrivals.ArrivalEntry
import com.eden.livewidget.ui.util.getResolvedOpenUriIntent
import java.time.format.DateTimeFormatter


@Composable
fun ArrivalsRepeatItem(
    arrival: ArrivalEntry,
    timeFormatter: DateTimeFormatter,
) {
    val openUrlAction = remember(arrival) {
        val (intent, bundle) =
            if (arrival.detailUri != null)
                getResolvedOpenUriIntent(arrival.detailUri)
            else Pair(Intent(), null)
        @OptIn(ExperimentalGlanceApi::class)
        actionStartActivity(
            intent = intent,
            activityOptions = bundle
        )
    }

    Box(
        modifier = GlanceModifier
            .clickable(onClick = openUrlAction),
    ) {

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(end = 32.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(44.dp)
                    .background(GlanceTheme.colors.surface)
            ) {}
        }
        Box(
            modifier = GlanceModifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = GlanceModifier
                    .width(78.dp)
                    .fillMaxHeight()
            ) {
                IdentifierColumn()
            }
        }
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = 80.dp, end = 64.dp)
        ) {
            DirectionBlock()
        }

        Box(
            modifier = GlanceModifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = GlanceModifier
                    .width(62.dp)
                    .fillMaxHeight()
            ) {

                TimeColumn(arrival, timeFormatter)
            }
        }
    }
}

@Composable
private fun TimeColumn(
    arrival: ArrivalEntry,
    timeFormatter: DateTimeFormatter
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(22.dp)
            .cornerRadius(12.dp)
            .background(GlanceTheme.colors.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = GlanceModifier.fillMaxWidth(),
                text = arrival.expectedDateTime.format(timeFormatter),
                style = TextStyle(
                    color = GlanceTheme.colors.onSecondaryContainer,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DirectionBlock() {

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(22.dp)
            .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {}
}

@Composable
private fun IdentifierColumn() {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(22.dp)
            .cornerRadius(8.dp)
            .padding(start = 8.dp, end = 8.dp)
            .background(GlanceTheme.colors.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {}
}