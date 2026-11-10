package com.eden.livewidget.data.version.repository

import kotlinx.coroutines.flow.Flow

interface VersionRepository {

    val appName: String
    val applicationId: String
    val versionName: String
    val versionCode: Int

    val isVersionCodeGreaterThanStored: Flow<Boolean?>

    suspend fun storeBuildVersionCode()
}