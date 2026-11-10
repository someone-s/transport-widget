package com.eden.livewidget.ui.main.datasync

import androidx.lifecycle.ViewModel
import com.eden.livewidget.data.agency.Agency

class DataSyncViewModel(
    val agencies: List<Agency>,
): ViewModel()
