package com.eden.livewidget.widget.ui

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import com.eden.livewidget.data.arrivals.ArrivalModel
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ArrivalsList(arrivals: List<ArrivalModel>) {

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(arrivals.take(20)) { index, arrival ->
            Column {
                ArrivalsItem(arrival, timeFormatter)

                if (index < arrivals.size - 1)
                    Spacer(modifier = GlanceModifier.height(4.dp))
            }

        }
        item {
            Spacer(modifier = GlanceModifier.height(16.dp))
        }
    }


}

