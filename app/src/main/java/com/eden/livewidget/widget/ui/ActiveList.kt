package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.eden.livewidget.R
import com.eden.livewidget.data.arrivals.ArrivalModel

@Composable
fun ActiveList(latestArrivals: List<ArrivalModel>) {

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
                        text = arrival.serviceName,
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