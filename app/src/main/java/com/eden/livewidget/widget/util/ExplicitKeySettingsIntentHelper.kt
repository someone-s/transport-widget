package com.eden.livewidget.widget.util

import android.content.Context
import android.content.Intent
import com.eden.livewidget.Agency
import com.eden.livewidget.main.MainActivity

fun getExplicitKeySettingsIntent(context: Context, agency: Agency): Intent {

    return Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.AGENCY_EXTRA_NAME, agency)
    }
}