package com.eden.livewidget.data.common.points

import kotlinx.serialization.Polymorphic

/**
 * Inherit this class and add @Serializable to the subclass
 */
@Polymorphic
interface Value {

    val displayString: String

}