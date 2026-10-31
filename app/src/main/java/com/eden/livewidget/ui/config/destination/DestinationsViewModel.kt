package com.eden.livewidget.ui.config.destination

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.livewidget.data.common.PointsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DestinationsViewModel(
    private val pointsRepository: PointsRepository,
): ViewModel() {
    val destinationsUiState: StateFlow<DestinationsUiState> =
        pointsRepository.matchingPoints.map { DestinationsUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = DestinationsUiState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

@Immutable
data class DestinationsUiState(val matchingPoints: List<PointsRepository.Option> = listOf())
