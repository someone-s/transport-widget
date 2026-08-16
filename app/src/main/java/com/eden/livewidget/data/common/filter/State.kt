package com.eden.livewidget.data.common.filter

import com.google.gson.Gson
import com.eden.livewidget.data.common.filter.destination.Filter as DestinationFilter

data class State(
    val destinationFilters: List<DestinationFilter>,
) {
    companion object {
        private val gson = Gson()

        fun serialize(list: State): String = gson.toJson(list)

        fun deserialize(string: String): State = gson.fromJson(string, State::class.java)
    }
}

fun emptyState() = State(
    destinationFilters = emptyList()
)