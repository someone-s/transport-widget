package com.eden.livewidget.main.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.R
import com.eden.livewidget.ui.theme.TransportWidgetsTheme


// context nullable for preview only
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(context: Context?) {

    Column {

        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.navigation_about),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(
                    top = 32.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            )
        }

        AboutPanel(
            stringResource(R.string.about_data_source_title),
            stringResource(R.string.about_data_source_body)
        ) {
            if (context == null) return@AboutPanel

            val uri = context.getString(R.string.about_data_source_url).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
        Spacer(Modifier.height(8.dp))

        AboutPanel(
            stringResource(R.string.about_user_interface_title),
            stringResource(R.string.about_user_interface_body)
        ) {
            if (context == null) return@AboutPanel

            val uri = context.getString(R.string.about_user_interface_url).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
        Spacer(Modifier.height(8.dp))

        AboutPanel(
            stringResource(R.string.about_caching_title),
            stringResource(R.string.about_caching_body)
        ) {
            if (context == null) return@AboutPanel

            val uri = context.getString(R.string.about_caching_url).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
        Spacer(Modifier.height(8.dp))

        AboutPanel(
            stringResource(R.string.about_source_code_title),
            stringResource(R.string.about_source_code_body)
        ) {
            if (context == null) return@AboutPanel

            val uri = context.getString(R.string.about_source_code_url).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }
}

@Composable
private fun AboutPanel(title: String, body: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween

        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(body)
        }
    }
}

@PreviewScreenSizes
@Composable
fun AboutScreenPreview() {
    val navController = rememberNavController()

    TransportWidgetsTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = Providers, Modifier.padding(innerPadding)) {
                composable<Providers> { AboutScreen(null) }
            }
        }
    }
}