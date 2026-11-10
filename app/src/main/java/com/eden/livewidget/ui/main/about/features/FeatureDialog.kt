package com.eden.livewidget.ui.main.about.features

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.eden.livewidget.R

@Composable
fun FeatureDialog(
    feature: Feature,
    onDismiss: () -> Unit,
) {
    val mediaItem = remember(feature.videoRes) {
        if (feature.videoRes != null) {
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .path(feature.videoRes.toString())
                .build()

            MediaItem.Builder()
                .setUri(uri)
                .build()
        } else null
    }
    val context = LocalContext.current
    val player = remember<Player?>(context, mediaItem) {
        if (mediaItem != null)
            ExoPlayer.Builder(context).build().also {
                it.setMediaItem(mediaItem)
                it.prepare()
                it.playWhenReady = true
                it.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            }
        else null
    }

    val hapticFeedback = LocalHapticFeedback.current

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                PlayerSurface(
                    player = player,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    surfaceType = SURFACE_TYPE_TEXTURE_VIEW
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(feature.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(feature.longTextRes),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OutlinedButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onDismiss()
                            },
                            content = { Text(text = stringResource(R.string.ui_main_about_features_featuredialog_dismiss)) }
                        )
                    }
                }
            }
        }
    }
}