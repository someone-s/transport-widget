package com.eden.livewidget.data.buffered

interface BufferedPointsCache {

    suspend fun getAllFuzzyMatches(search: String): List<BufferedPointOption>

    suspend fun insert(option: BufferedPointOption)
    suspend fun deleteAll()
    
    val asResettable: Resettable?
        get() = this as? Resettable

    interface Resettable {
        suspend fun reset()
    }
}