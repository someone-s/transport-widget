package com.eden.livewidget.data.version.repository

import android.content.Context
import androidx.core.content.edit
import com.eden.livewidget.BuildConfig
import com.eden.livewidget.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class OfflineVersionRepository(
    private val context: Context
): VersionRepository {

    override val appName: String = context.getString(R.string.app_name)
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: Int = BuildConfig.VERSION_CODE

    companion object {
        const val VERSION_PREFERENCES = "${BuildConfig.APPLICATION_ID}.VERSION_PREFERENCES"
        const val PREFERENCE_VERSION_CODE = "${BuildConfig.APPLICATION_ID}.PREFERENCE_VERSION_CODE"
    }

    private val _isVersionCodeGreaterThanStored = MutableStateFlow(calculateIsVersionCodeGreaterThanStored())
    override val isVersionCodeGreaterThanStored: Flow<Boolean?> = _isVersionCodeGreaterThanStored

    private val storedVersionCode: Int? get() {
        val preferences = context.getSharedPreferences(VERSION_PREFERENCES, Context.MODE_PRIVATE)
        val storedValue = preferences.getInt(PREFERENCE_VERSION_CODE, -1)
        return if (storedValue < 0) null else storedValue
    }

    override suspend fun storeBuildVersionCode() {
        val preferences = context.getSharedPreferences(VERSION_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit {
            putInt(PREFERENCE_VERSION_CODE, versionCode)
        }
        _isVersionCodeGreaterThanStored.update { calculateIsVersionCodeGreaterThanStored() }
    }

    private fun calculateIsVersionCodeGreaterThanStored(): Boolean? {
        val s = storedVersionCode
        return if (s != null) versionCode > s else null
    }


}