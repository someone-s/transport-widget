package com.eden.livewidget.data.location.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.eden.livewidget.data.location.structure.Location
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

open class DeviceLocationRepository(
    private val context: Context
): LocationRepository {

    override suspend fun getLocation(accuracy: LocationRepository.Accuracy): Location {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?:
            throw LocationRepository.UnavailableException()

        if (!LocationManagerCompat.isLocationEnabled(manager))
            throw LocationRepository.UnavailableException()

        val permission = when (accuracy) {
            LocationRepository.Accuracy.FINE -> Manifest.permission.ACCESS_FINE_LOCATION
            LocationRepository.Accuracy.COARSE -> Manifest.permission.ACCESS_COARSE_LOCATION
        }
        when (ContextCompat.checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_DENIED -> throw LocationRepository.MissingPermissionException()
        }

        val location =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                getLocationFromProvider(manager, LocationManager.FUSED_PROVIDER)
            else null) ?:
            getLocationFromProvider(manager, LocationManager.NETWORK_PROVIDER) ?:
            getLocationFromProvider(manager, LocationManager.GPS_PROVIDER) ?:
            throw LocationRepository.UnavailableException()
        return location
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private suspend fun getLocationFromProvider(manager: LocationManager, provider: String): Location? {

        if (!LocationManagerCompat.hasProvider(manager, provider))
            return null
        if (!manager.isProviderEnabled(provider))
            return null

        return suspendCancellableCoroutine { continuation ->

            val cancellationSignal = CancellationSignal()

            continuation.invokeOnCancellation { cancellationSignal.cancel() }

            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(context)
            ) {
                continuation.resume(it?.let { Location(it.latitude, it.longitude) })
            }
        }
    }

    override fun getLastKnownLocation(): Location {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?:
        throw LocationRepository.UnavailableException()

        val hasFineLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFineLocation && !hasCoarseLocation)
            throw LocationRepository.MissingPermissionException()

        val location =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                getLastKnownLocationFromProvider(manager, LocationManager.FUSED_PROVIDER)
            else null) ?:
            getLastKnownLocationFromProvider(manager, LocationManager.NETWORK_PROVIDER) ?:
            getLastKnownLocationFromProvider(manager, LocationManager.GPS_PROVIDER) ?:
            throw LocationRepository.UnavailableException()
        return location
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun getLastKnownLocationFromProvider(manager: LocationManager, provider: String): Location? {

        if (!LocationManagerCompat.hasProvider(manager, provider))
            return null
        if (!manager.isProviderEnabled(provider))
            return null

        return manager.getLastKnownLocation(provider)?.let { Location(it.latitude, it.longitude) }
    }
}