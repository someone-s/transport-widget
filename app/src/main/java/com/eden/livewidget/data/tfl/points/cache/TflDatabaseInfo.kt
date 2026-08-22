package com.eden.livewidget.data.tfl.points.cache

import com.eden.livewidget.data.common.points.cache.DatabaseInfo

class TflDatabaseInfo: DatabaseInfo {
    override val databaseName: String
        get() = "TFL_CACHE_DATABASE"

    override val populateFileName: String
        get() = "TFL_CACHE_DATABASE"
}