package com.eden.livewidget.ui.main.about.support

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.livewidget.LiveWidgetApplication

object SupportViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = mainApplication()
            val container = application.container
            SupportViewModel(
                container.versionRepository,
            )
        }
    }
}

private fun CreationExtras.mainApplication(): LiveWidgetApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LiveWidgetApplication