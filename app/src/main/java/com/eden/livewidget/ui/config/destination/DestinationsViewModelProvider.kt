package com.eden.livewidget.ui.config.destination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.livewidget.LiveWidgetApplication

class DestinationsViewModelFactory(
    private val agencyId: String,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (!modelClass.isAssignableFrom(DestinationsViewModel::class.java))
            throw IllegalArgumentException("Unexpected ViewModel type")
        val application = extras.mainApplication()
        val container = application.container
        val agency =
            container.agencyFromId(agencyId) ?: throw IllegalArgumentException("Invalid agency")
        @Suppress("UNCHECKED_CAST")
        return DestinationsViewModel(agency.provider.pointsRepository) as T
    }
}

private fun CreationExtras.mainApplication(): LiveWidgetApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LiveWidgetApplication