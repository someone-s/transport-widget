package com.eden.livewidget.ui.util

import android.content.Context
import android.content.Intent
import com.eden.livewidget.Agency
import com.eden.livewidget.ui.main.MainActivity

fun getExplicitKeySettingsIntent(context: Context, agency: Agency): Intent {

    return Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.AGENCY_EXTRA_NAME, agency)
    }
}