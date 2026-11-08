package com.eden.livewidget.ui.main.about.support

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.livewidget.R
import com.eden.livewidget.ui.common.components.LabelItem

data class Link(
    @StringRes
    val titleRes: Int,
    @StringRes
    val supportTextRes: Int,
    val uri: Uri,
)

@Composable
fun rememberSupportSectionState(
    key1: Any? = null
): SupportSectionState {
    return remember(key1) { SupportSectionState() }
}
@Stable
class SupportSectionState(
    val links: List<Link> = listOf(
        Link(
            titleRes = R.string.ui_main_about_features_supportsection_upcoming_title,
            supportTextRes = R.string.ui_main_about_features_supportsection_upcoming_support,
            uri = "https://github.com/someone-s/transport-widget/issues".toUri()
        ),
        Link(
            titleRes = R.string.ui_main_about_features_supportsection_issues_title,
            supportTextRes = R.string.ui_main_about_features_supportsection_issues_support,
            uri = "https://github.com/someone-s/transport-widget/issues/new".toUri()
        ),
        Link(
            titleRes = R.string.ui_main_about_features_supportsection_releases_title,
            supportTextRes = R.string.ui_main_about_features_supportsection_releases_support,
            uri = "https://github.com/someone-s/transport-widget/releases".toUri()
        ),
    )
)


@Composable
fun SupportSection(
    modifier: Modifier = Modifier,
    state: SupportSectionState = rememberSupportSectionState(),
    viewModel: SupportViewModel =
        if (LocalInspectionMode.current) mockSupportViewModel
        else viewModel(factory = SupportViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))
        LabelItem(text = stringResource(R.string.ui_main_about_supportsection_label))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            state.links.forEachIndexed { index, link ->
                SegmentedListItem(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        context.startActivity(CustomTabsIntent.Builder()
                            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                            .build()
                            .intent
                            .apply {
                                data = link.uri
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            }
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index, state.links.size),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    content = { Text(text = stringResource(link.titleRes)) },
                    supportingContent = { Text(text = stringResource(link.supportTextRes)) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${viewModel.applicationId} ${viewModel.versionName} (${viewModel.versionCode})",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
