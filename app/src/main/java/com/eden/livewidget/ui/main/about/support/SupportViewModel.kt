package com.eden.livewidget.ui.main.about.support

import androidx.lifecycle.ViewModel
import com.eden.livewidget.data.version.repository.VersionRepository

class SupportViewModel(
    versionRepository: VersionRepository
): ViewModel() {
    val applicationId: String = versionRepository.applicationId
    val versionName: String = versionRepository.versionName
    val versionCode: Int = versionRepository.versionCode
}