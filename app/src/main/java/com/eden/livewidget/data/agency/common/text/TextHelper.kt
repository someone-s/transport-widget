package com.eden.livewidget.data.agency.common.text

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

fun Text.asString(context: Context) = when(this) {
    is LiteralText -> this.value
    is ResourceText -> context.getString(this.value)
    else -> throw IllegalStateException("Unknown text type.")
}

@Composable
fun Text.asString() = when(this) {
    is LiteralText -> this.value
    is ResourceText -> stringResource(this.value)
    else -> throw IllegalStateException("Unknown text type.")
}