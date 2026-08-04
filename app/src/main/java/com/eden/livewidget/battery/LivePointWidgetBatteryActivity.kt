package com.eden.livewidget.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.eden.livewidget.battery.ui.BatteryPrompt
import com.eden.livewidget.ui.theme.TransportWidgetsTheme

class LivePointWidgetBatteryActivity: ComponentActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TransportWidgetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BatteryPrompt(this) { finishAffinity() }
                }

            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        finishAffinity()
    }
}