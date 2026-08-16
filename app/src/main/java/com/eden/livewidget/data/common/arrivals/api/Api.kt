package com.eden.livewidget.data.common.arrivals.api

import android.content.Context
import com.eden.livewidget.data.common.arrivals.Model

interface Api {

    @Throws(
        UnresolvedException::class,
        UnreachableException::class,
        AuthenticationException::class
    )
    suspend fun fetchLatestArrivals(context: Context): List<Model>

    class UnresolvedException(message: String?) : RuntimeException(message)
    class UnreachableException(message: String?) : RuntimeException(message)
    class AuthenticationException(message: String?) : RuntimeException(message)
}