package com.eden.livewidget.main.ui.datasync

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eden.livewidget.Agency

@Composable
fun SourceContainer(
    context: Context?,
    agency: Agency,
    setCurrentInputKeyAction: (((String) -> Unit)?) -> Unit,
    setCurrentKeyValueAction: (String) -> Unit,
    setInputKeyState: (Boolean) -> Unit,
    setCurrentDownloadAction: ((() -> Unit)?) -> Unit,
    setDownloadWarningState: (Boolean) -> Unit,
    setCurrentResetAction: ((() -> Unit)?) -> Unit,
    setResetWarningState: (Boolean) -> Unit,
) {


    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = stringResource(agency.agencyName),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(agency.agencyDescription),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Justify
            )
            Spacer(Modifier.height(8.dp))


            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                val showKeyConfig = agency.apiProvider.keyProviders.isNotEmpty()
                if (showKeyConfig) {
                    SourceKeyConfigItems(
                        context,
                        agency,
                        setCurrentInputKeyAction,
                        setCurrentKeyValueAction,
                        setInputKeyState,
                        ListItemDefaults.segmentedShapes(0, 3),
                    )
                }
                SourceDownloadItems(
                    context,
                    agency,
                    setCurrentDownloadAction,
                    setDownloadWarningState,
                    if (showKeyConfig)
                        ListItemDefaults.segmentedShapes(1, 3)
                    else
                        ListItemDefaults.segmentedShapes(0, 2),
                )
                SourceResetItem(
                    context,
                    agency,
                    setCurrentResetAction,
                    setResetWarningState,
                    if (showKeyConfig)
                        ListItemDefaults.segmentedShapes(2, 3)
                    else
                        ListItemDefaults.segmentedShapes(1, 2),
                )
            }
        }
    }
}

