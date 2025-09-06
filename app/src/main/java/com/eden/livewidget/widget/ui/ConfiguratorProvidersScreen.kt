package com.eden.livewidget.widget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.data.points.PointsRepository
import com.eden.livewidget.data.Provider
import com.eden.livewidget.ui.component.CustomizableSearchBar
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguratorSelectProviderScreen(
    navController: NavController,
    apiProvider: MutableState<Provider>
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    // Controls expansion state of the search bar
    val textFieldState = rememberTextFieldState()

    val repository = remember(key1 = apiProvider) { PointsRepository.getInstance(context, apiProvider.value) }
    val matchingProviders by repository.matchingPoints.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CustomizableSearchBar(
                    onQueryChange = {
                        textFieldState.edit { replace(0, length, it) }
                        coroutineScope.launch {
                            val repository =
                                PointsRepository.getInstance(context, apiProvider.value)
                            repository.fetchMatching(it)
                        }
                    },
                    query = textFieldState.text.toString(),
                    onSearch = {},
                    searchResults = matchingProviders.map { points -> points.name },
                    onResultClick = {_, _ -> },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) { contentPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding)
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                        }
                    }
                    itemsIndexed(matchingProviders) { index, item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(
                                topStart = if (index == 0) 8.dp else 2.dp,
                                topEnd = if (index == 0) 8.dp else 2.dp,
                                bottomStart = if (index == matchingProviders.size - 1) 8.dp else 2.dp,
                                bottomEnd = if (index == matchingProviders.size - 1) 8.dp else 2.dp
                            )
                        ) {

                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.name,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        }
                    }
                }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewConfiguratorSelectProviderScreen() {

    TransportWidgetsTheme {

        ConfiguratorSelectProviderScreen(rememberNavController(), remember { mutableStateOf(Provider.TFL) })
    }
}