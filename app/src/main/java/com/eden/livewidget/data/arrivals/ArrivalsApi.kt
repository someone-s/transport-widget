package com.eden.livewidget.data.arrivals

import android.content.Context


interface ArrivalsApi {

    @Throws(
        UnresolvedException::class,
        UnreachableException::class,
        AuthenticationException::class
    )
    suspend fun fetchLatestArrivals(context: Context): List<ArrivalModel>

    class UnresolvedException(message: String?) : RuntimeException(message)
    class UnreachableException(message: String?) : RuntimeException(message)
    class AuthenticationException(message: String?) : RuntimeException(message)
}


