package com.eden.livewidget.data.keys

import java.util.EnumSet

typealias KeyProviderConstructors = Map<EnumSet<KeyPurpose>, () -> KeyProvider>

fun KeyProviderConstructors.getKeyProviderConstructor(purpose: KeyPurpose): (() -> KeyProvider)? {
    return this.firstNotNullOfOrNull { entry -> if (entry.key.contains(purpose)) entry.value else null }
}