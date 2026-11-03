package com.eden.livewidget.ui.config

import androidx.lifecycle.ViewModel
import com.eden.livewidget.data.AppContainer
import com.eden.livewidget.data.widget.structure.WidgetConfig

class ConfigViewModel(
    private val appContainer: AppContainer,
): ViewModel() {

    val agencies = appContainer.agencies
    fun agencyFromId(id: String) = appContainer.agencyFromId(id)

    suspend fun getWidgetConfig(appWidgetId: Int): WidgetConfig? =
        appContainer.widgetRepository.getConfig(appWidgetId)

    suspend fun setWidgetConfig(config: WidgetConfig) {
        appContainer.widgetRepository.setConfig(config)
    }
}