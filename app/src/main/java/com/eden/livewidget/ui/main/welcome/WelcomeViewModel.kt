package com.eden.livewidget.ui.main.welcome

import androidx.lifecycle.ViewModel
import com.eden.livewidget.data.version.repository.VersionRepository
import kotlinx.coroutines.flow.Flow

class WelcomeViewModel(
    private val versionRepository: VersionRepository
): ViewModel() {
    val appName: String = versionRepository.appName
    val versionName: String = versionRepository.versionName
    val isVersionCodeGreaterThanStored: Flow<Boolean?> = versionRepository.isVersionCodeGreaterThanStored
    suspend fun storeBuildVersionCode() { versionRepository.storeBuildVersionCode() }
}