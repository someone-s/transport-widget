package com.eden.livewidget.data.common

import kotlinx.serialization.Polymorphic

/**
 * Inherit this class and add @Serializable to the subclass
 */
@Polymorphic
interface DataPoint {

    val displayString: String

}