package com.eden.livewidget.ui.main.datasync

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.eden.livewidget.R

@Composable
fun DataSyncTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    LargeFlexibleTopAppBar(
        title = {
            val textStyle = LocalTextStyle.current
            val imageSize = with(LocalDensity.current) { textStyle.lineHeight.toDp() }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    painter = painterResource(R.drawable.corporate_fare),
                    contentDescription = null,
                    modifier = Modifier.size(imageSize)
                )
                Spacer(Modifier.width(imageSize * 0.25f))
                Text(
                    text = stringResource(R.string.ui_main_navigation_toplevelroute_providers),
                    fontWeight = FontWeight.SemiBold,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(imageSize * 0.4f))
            }
        },
        titleHorizontalAlignment = Alignment.CenterHorizontally,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        scrollBehavior = scrollBehavior,
    )
}