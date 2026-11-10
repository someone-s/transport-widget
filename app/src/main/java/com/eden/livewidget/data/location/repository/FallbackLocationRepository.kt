package com.eden.livewidget.data.location.repository

import android.content.Context
import com.eden.livewidget.data.location.structure.Location

class FallbackLocationRepository(
    context: Context,
    initialFallbackLocation: Location? = null
): DeviceLocationRepository(context) {

    private var currentLocationFallback = initialFallbackLocation

    override suspend fun getLocation(accuracy: LocationRepository.Accuracy): Location =
        try {
            val location = super.getLocation(accuracy)
            currentLocationFallback = location
            location
        } catch (e: Exception) {
            currentLocationFallback ?: throw e
        }

    private var lastKnownLocationFallback = initialFallbackLocation

    override fun getLastKnownLocation(): Location =
        try {
            val newLocation = super.getLastKnownLocation()
            lastKnownLocationFallback = newLocation
            newLocation
        } catch (e: Exception) {
            lastKnownLocationFallback ?: throw e
        }
}