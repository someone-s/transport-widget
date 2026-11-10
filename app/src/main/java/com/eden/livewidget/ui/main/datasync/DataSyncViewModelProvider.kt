package com.eden.livewidget.ui.main.datasync

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.livewidget.LiveWidgetApplication

object DataSyncViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = mainApplication()
            val container = application.container
            DataSyncViewModel(container.agencies)
        }
    }
}

private fun CreationExtras.mainApplication(): LiveWidgetApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LiveWidgetApplication