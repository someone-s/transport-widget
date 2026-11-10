package com.eden.livewidget.ui.main.datasync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.data.agency.mockAgency
import com.eden.livewidget.ui.main.datasync.acknowledgment.SourceAcknowledgmentItem
import com.eden.livewidget.ui.main.datasync.download.SourceDownloadItems
import com.eden.livewidget.ui.main.datasync.key.SourceKeyConfigItems
import com.eden.livewidget.ui.main.datasync.reset.SourceResetItem

@Composable
fun SourceContainer(
    agency: Agency,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val targetAgencyId = LocalTargetAgencyId.current
    var ranOnStart by rememberSaveable(targetAgencyId) { mutableStateOf(false) }
    LaunchedEffect(ranOnStart) {
        if (ranOnStart) return@LaunchedEffect

        if (targetAgencyId == agency.id)
            isExpanded = true

        ranOnStart = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween

        ) {

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(end = 48.dp),
                ) {
                    Text(
                        text = stringResource(agency.agencyName),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(agency.agencyDescription),
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    FilledIconButton(
                        onClick = { isExpanded = !isExpanded },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        content = {
                            if (isExpanded)
                                Icon(
                                    painter = painterResource(R.drawable.ic_shared_arrow_drop_up),
                                    contentDescription = stringResource(R.string.ui_main_datasync_sorucecontainer_collapse)
                                )
                            else
                                Icon(
                                    painter = painterResource(R.drawable.ic_shared_arrow_drop_down),
                                    contentDescription = stringResource(R.string.ui_main_datasync_sorucecontainer_expand)
                                )
                        }
                    )
                }
            }

            AnimatedVisibility(isExpanded) {

                val showKeyConfig = agency.provider.keys.isNotEmpty()

                val showDownload = agency.provider.pointsRepository.asRefreshable != null
                val showReset = agency.provider.pointsRepository.asResettable != null

                val count =
                    1 + (if (showKeyConfig) 1 else 0) + (if (showDownload) 1 else 0) + (if (showReset) 1 else 0)
                val acknowledgmentIndex = 0
                val keyIndex = 1
                val downloadIndex = 2 + (if (showKeyConfig) 0 else -1)
                val resetIndex =
                    3 + (if (showKeyConfig) 0 else -1) + (if (showDownload) 0 else -1)


                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    Spacer(Modifier.height(8.dp))

                    SourceAcknowledgmentItem(
                        agency,
                        ListItemDefaults.segmentedShapes(acknowledgmentIndex, count),
                    )

                    if (showKeyConfig) {
                        SourceKeyConfigItems(
                            agency,
                            ListItemDefaults.segmentedShapes(keyIndex, count),
                        )
                    }
                    if (showDownload)
                        SourceDownloadItems(
                            agency,
                            ListItemDefaults.segmentedShapes(downloadIndex, count),
                        )
                    if (showReset)
                        SourceResetItem(
                            agency,
                            ListItemDefaults.segmentedShapes(resetIndex, count),
                        )
                }
            }
        }
    }
}

@Preview(
    widthDp = 400,
    heightDp = 200,
)
@Composable
fun SourceContainerPreview() {
    SourceContainer(mockAgency)
}
