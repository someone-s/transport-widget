package com.eden.livewidget.data.agency.common.point

import kotlinx.serialization.Polymorphic

/**
 * Inherit this class and add @Serializable to the subclass
 */
@Polymorphic
interface DataPoint {

    val displayString: String

}