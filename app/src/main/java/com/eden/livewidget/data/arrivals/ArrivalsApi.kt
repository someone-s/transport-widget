package com.eden.livewidget.data.arrivals

import android.content.Context


interface ArrivalsApi {

    @Throws(UnreachableException::class, AuthenticationException::class)
    suspend fun fetchLatestArrivals(context: Context): List<ArrivalModel>

    companion object {
        class UnreachableException(message: String?) : RuntimeException(message)
        class AuthenticationException(message: String?) : RuntimeException(message)
    }
}


