package com.eden.livewidget

import android.app.Application
import com.eden.livewidget.data.AppContainer
import com.eden.livewidget.data.AppDataContainer

class LiveWidgetApplication: Application() {

    lateinit var container: AppContainer


    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(context = this)
    }
}