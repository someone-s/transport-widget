package com.eden.livewidget.data.common.points.cache

interface DatabaseInfo {
    val databaseName: String

    /**
     * File name of SQL to pre-populate the database onCreate or onDestructiveMigration
     * Must be a file assets/database/{populateFileName}
     */
    val populateFileName: String?
}