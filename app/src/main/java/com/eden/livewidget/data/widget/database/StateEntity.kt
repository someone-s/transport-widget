package com.eden.livewidget.data.widget.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "state",
    foreignKeys = [
        ForeignKey(
            entity = ConfigEntity::class,
            parentColumns = ["app_widget_id"],
            childColumns = ["app_widget_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class StateEntity(
    @PrimaryKey
    @ColumnInfo(name = "app_widget_id")
    val appWidgetId: Int,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,
    @ColumnInfo(name = "fetch_state")
    val fetchState: String = PENDING,
) {
    companion object {
        const val PENDING = "PENDING"
        const val RESULT_ERROR_UNKNOWN = "ERROR_UNKNOWN"
        const val RESULT_ERROR_NETWORK_AUTHENTICATION = "ERROR_NETWORK_AUTHENTICATION"
        const val RESULT_ERROR_NETWORK_UNREACHABLE = "ERROR_NETWORK_UNREACHABLE"
        const val RESULT_ERROR_NETWORK_UNRESOLVED = "ERROR_NETWORK_UNRESOLVED"
        const val RESULT_ERROR_NETWORK_METERED = "ERROR_NETWORK_METERED"
        const val RESULT_ERROR_BATTERY = "ERROR_BATTERY"
        const val RESULT_ERROR_LOCATION_DISABLED = "ERROR_LOCATION_DISABLED"
        const val RESULT_ERROR_LOCATION_PERMISSION = "ERROR_LOCATION_PERMISSION"
        const val RESULT_RAN_COMPLETED = "RAN_COMPLETED"
    }
}