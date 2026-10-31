package com.eden.livewidget.data.keys

import java.util.EnumSet

data class Key(
    val purposes: EnumSet<KeyPurpose>,
    val provider: KeyProvider,
)