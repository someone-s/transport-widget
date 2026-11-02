package com.eden.livewidget.ui.config.destination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.eden.livewidget.data.common.Provider

class DestinationsViewModelFactory(
    private val provider: Provider,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (!modelClass.isAssignableFrom(DestinationsViewModel::class.java))
            throw IllegalArgumentException("Unexpected ViewModel type")
        @Suppress("UNCHECKED_CAST")
        return DestinationsViewModel(provider.pointsRepository) as T
    }
}