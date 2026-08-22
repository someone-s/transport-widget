package com.eden.livewidget.config.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun HiddenSearchBar(
    state: SearchBarState,
    inputField: @Composable () -> Unit,
) {


    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp)
            .semantics { hideFromAccessibility() },
        state = state,
        inputField = inputField
    )
}