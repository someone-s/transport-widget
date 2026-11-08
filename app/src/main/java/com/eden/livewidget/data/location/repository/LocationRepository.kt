package com.eden.livewidget.data.location.repository

import com.eden.livewidget.data.location.structure.Location

interface LocationRepository {

    enum class Accuracy { FINE, COARSE }

    @Throws(MissingPermissionException::class, UnavailableException::class)
    suspend fun getLocation(accuracy: Accuracy): Location

    class MissingPermissionException: RuntimeException()
    class UnavailableException: RuntimeException()
}