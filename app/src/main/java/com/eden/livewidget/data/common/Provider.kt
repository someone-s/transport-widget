package com.eden.livewidget.data.common

import android.net.Uri

interface Provider {
    val agencyName: Int
    val agencyShortDescription: Int
    val agencyDescription: Int
    val apiProvider: Provider
    val agencyHelp: Uri
    val pointsRepository: PointsRepository
    val arrivalsRepository: ArrivalsRepository
}