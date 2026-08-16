package com.eden.livewidget.data.tfl.filter.destination

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LineWithDirection(
    val lineId: String,
    val direction: String,
) {
    companion object {
        private val gson = Gson()
        private val itemType = object : TypeToken<Collection<LineWithDirection>>() {}.type

        fun serializeList(list: Collection<LineWithDirection>): String = gson.toJson(list)

        fun deserializeList(string: String): Collection<LineWithDirection> = gson.fromJson(string, itemType)
    }
}

