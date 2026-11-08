package com.eden.livewidget.ui.main.about.features

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R
import com.eden.livewidget.ui.common.components.LabelItem

data class Feature(
    @StringRes
    val titleRes: Int,
    @StringRes
    val shortTextRes: Int,
    @StringRes
    val longTextRes: Int,
    @RawRes
    val videoRes: Int?,
)

@Composable
fun rememberFeaturesSectionState(
    key1: Any? = null
): FeaturesSectionState {
    return remember(key1) { FeaturesSectionState() }
}
@Stable
class FeaturesSectionState(
    val placeholder: Feature = Feature(
        titleRes = R.string.ui_main_about_features_featuressection_placeholder,
        shortTextRes = R.string.ui_main_about_features_featuressection_placeholder,
        longTextRes = R.string.ui_main_about_features_featuressection_placeholder,
        videoRes = null,
    ),
    val features: List<Feature> = listOf(
        Feature(
            titleRes = R.string.ui_main_about_features_featuressection_datasource_title,
            shortTextRes = R.string.ui_main_about_features_featuressection_datasource_short,
            longTextRes = R.string.ui_main_about_features_featuressection_datasource_long,
            videoRes = null,
        ),
        Feature(
            titleRes = R.string.ui_main_about_features_featuressection_filterdestination_title,
            shortTextRes = R.string.ui_main_about_features_featuressection_filterdestination_short,
            longTextRes = R.string.ui_main_about_features_featuressection_filterdestination_long,
            videoRes = null,
        ),
        Feature(
            titleRes = R.string.ui_main_about_features_featuressection_livelocation_title,
            shortTextRes = R.string.ui_main_about_features_featuressection_livelocation_short,
            longTextRes = R.string.ui_main_about_features_featuressection_livelocation_long,
            videoRes = null,
        ),
    )
) {
    var visible by mutableStateOf(false)
    var selectedIndex by mutableIntStateOf(-1)

    val targetFeature by derivedStateOf { if (selectedIndex >= 0 && selectedIndex < features.size) features[selectedIndex] else placeholder }
}

@Composable
fun FeaturesSection(
    modifier: Modifier = Modifier,
    state: FeaturesSectionState = rememberFeaturesSectionState()
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))
        LabelItem(text = stringResource(R.string.ui_main_about_featuressection_label))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            state.features.forEachIndexed { index, feature ->
                SegmentedListItem(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        state.selectedIndex = index
                        state.visible = true
                    },
                    shapes = ListItemDefaults.segmentedShapes(index, state.features.size),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    content = { Text(text = stringResource(feature.titleRes)) },
                    supportingContent = { Text(text = stringResource(feature.shortTextRes)) },
                )
            }
        }
    }

    AnimatedVisibility(state.visible) {
        FeatureDialog(
            feature = state.targetFeature,
            onDismiss = { state.visible = false },
        )
    }
}


@Preview(widthDp = 400, heightDp = 400)
@Composable
fun FeatureSectionPreview() {

    val state = rememberFeaturesSectionState().also {
        it.visible = true
    }

    FeaturesSection(
        modifier = Modifier.fillMaxSize(),
        state = state,
    )
}
