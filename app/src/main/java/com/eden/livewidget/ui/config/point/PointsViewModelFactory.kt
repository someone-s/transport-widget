package com.eden.livewidget.ui.config.point

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.eden.livewidget.data.common.Provider

class PointsViewModelFactory(
    private val provider: Provider,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (!modelClass.isAssignableFrom(PointsViewModel::class.java))
            throw IllegalArgumentException("Unexpected ViewModel type")
        @Suppress("UNCHECKED_CAST")
        return PointsViewModel(provider.pointsRepository) as T
    }
}