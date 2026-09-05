package com.eden.livewidget.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R
import com.eden.livewidget.data.common.arrivals.LocationBoard
import com.eden.livewidget.data.common.arrivals.LocationFrom
import com.eden.livewidget.data.common.arrivals.LocationVia
import com.eden.livewidget.data.common.arrivals.Model
import com.eden.livewidget.widget.util.getResolvedOpenUriIntent
import java.time.format.DateTimeFormatter

@Composable
fun ArrivalsItem(
    arrival: Model,
    timeFormatter: DateTimeFormatter,
) {
        val resolvedOpenUriIntent =
            if (arrival.detailUri != null)
                getResolvedOpenUriIntent(LocalContext.current.packageManager, arrival.detailUri)
            else
                Intent()

        Box(
            modifier = GlanceModifier
                .clickable(actionStartActivity(resolvedOpenUriIntent))
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
                    IdentifierColumn(arrival)

                }
            }
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = 80.dp, end = 64.dp)
            ) {

                DirectionBlock(arrival)

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
    arrival: Model,
    timeFormatter: DateTimeFormatter
) {
    Column {
        Box(
            modifier = GlanceModifier
                .size(62.dp)
                .cornerRadius(31.dp)
                .background(GlanceTheme.colors.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (arrival.remainingS < 60)
                Image(
                    provider = ImageProvider(R.drawable.ic_shared_filled_notification_active),
                    contentDescription = LocalContext.current.getString(R.string.widget_arrival_now_icon),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer)
                )
            else
                Text(
                    text = LocalContext.current.getString(
                        R.string.widget_arrival_minute_text,
                        (arrival.remainingS / 60)
                    ),
                    style = TextStyle(
                        color = GlanceTheme.colors.onTertiaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                    ),
                    maxLines = 1
                )
        }

        Spacer(
            modifier = GlanceModifier.height(2.dp)
        )
        Box(
            modifier = GlanceModifier
                .width(62.dp)
                .height(24.dp)
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
}

private enum class DirectionModes(val mask: Int) {
    NONE(mask = 0),
    TOP(mask = 1),
    BOTTOM(mask = 2),
    BOTH(mask = 3),
}
private fun DirectionModes.hasTop() = (this.mask and DirectionModes.TOP.mask) > 0
private fun DirectionModes.hasBottom() = (this.mask and DirectionModes.BOTTOM.mask) > 0

@Composable
private fun DirectionBlock(arrival: Model) {

    val mode =
        if (arrival.locationPretext != null && arrival.locationSupplement != null)
            DirectionModes.BOTH
        else if (arrival.locationPretext != null)
            DirectionModes.TOP
        else if (arrival.locationSupplement != null)
            DirectionModes.BOTTOM
        else
            DirectionModes.NONE

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp)
    ) {

        if (mode.hasTop()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                Row {
                    Text(
                        text = when (arrival.locationPretext!!) {
                            is LocationBoard -> arrival.locationPretext.boardText
                        },
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                        ),
                        maxLines = 1
                    )
                }
            }
        }
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(
                    top = if (mode.hasTop()) 28.dp else 0.dp,
                    bottom = if (mode.hasBottom() || !mode.hasTop()) 26.dp else 0.dp,
                ),
            contentAlignment = if (mode.hasTop()) Alignment.TopStart else Alignment.BottomStart
        ) {
            Text(
                text = arrival.destinationName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                maxLines = if (mode.hasTop() && mode.hasBottom()) 1 else 2
            )
        }
        if (mode.hasBottom()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Row {
                    Text(
                        text = when (arrival.locationSupplement!!) {
                            is LocationVia -> LocalContext.current.getString(R.string.widget_arrival_via_text)
                            is LocationFrom -> LocalContext.current.getString(R.string.widget_arrival_from_text)
                        },
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                        ),
                        maxLines = 1
                    )
                    Spacer(
                        modifier = GlanceModifier
                            .width(4.dp)
                    )
                    Text(
                        text = when (arrival.locationSupplement) {
                            is LocationVia -> arrival.locationSupplement.viaText
                            is LocationFrom -> arrival.locationSupplement.fromText
                        },
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun IdentifierColumn(arrival: Model) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(24.dp)
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
                Image(
                    provider = ImageProvider(R.drawable.ic_shared_filled_tapas),
                    contentDescription = LocalContext.current.getString(R.string.widget_platform_icon),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer),
                    modifier = GlanceModifier.size(12.dp)
                )
                Spacer(
                    modifier = GlanceModifier.width(2.dp)
                )
                Text(
                    modifier = GlanceModifier.fillMaxWidth(),
                    text = arrival.platformName ?: LocalContext.current.getString(R.string.widget_platform_platform_placeholder),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                    ),
                    maxLines = 1
                )
            }
        }
        Spacer(
            modifier = GlanceModifier.height(2.dp)
        )
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(8.dp)
                .padding(top = 0.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                .background(GlanceTheme.colors.primaryContainer),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = arrival.serviceName,
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
                maxLines = 1
            )
            Spacer(
                modifier = GlanceModifier.height(1.dp)
            )
            Text(
                text = arrival.operatorName,
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
                maxLines = 1
            )
        }

    }
}