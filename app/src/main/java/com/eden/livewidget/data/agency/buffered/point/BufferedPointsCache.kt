package com.eden.livewidget.data.agency.buffered.point

interface BufferedPointsCache {

    suspend fun getAllFuzzyMatches(search: String): List<BufferedPointEntry>

    suspend fun insert(option: BufferedPointEntry)
    suspend fun deleteAll()
    
    val asResettable: Resettable?
        get() = this as? Resettable

    interface Resettable {
        suspend fun reset()
    }
}