package com.eden.livewidget.data.rdg.points.cache

import com.eden.livewidget.data.common.points.cache.DatabaseInfo

class RdgDatabaseInfo: DatabaseInfo {
    override val databaseName: String
        get() = "RDG_CACHE_DATABASE"

    override val populateFileName: String
        get() = "RDG_CACHE_DATABASE"
}