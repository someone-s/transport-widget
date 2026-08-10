package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R

@Composable
fun FetchingSurface() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(all = 8.dp)
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = GlanceModifier.size(48.dp),
                provider = ImageProvider(R.drawable.ic_shared_outlined_clock_arrow_down),
                contentDescription = LocalContext.current.getString(R.string.widget_active_loading_icon),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground)
            )
            Text(
                text = LocalContext.current.getString(R.string.widget_active_loading_text),
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }
    }
}