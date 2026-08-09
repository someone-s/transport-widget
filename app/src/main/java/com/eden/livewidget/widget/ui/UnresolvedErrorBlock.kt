package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R

@Composable
fun UnresolvedErrorBlock() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        RetrySurface {
            Text(
                text = LocalContext.current.getString(R.string.widget_retry_unreachable_reason_text),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }
    }
}