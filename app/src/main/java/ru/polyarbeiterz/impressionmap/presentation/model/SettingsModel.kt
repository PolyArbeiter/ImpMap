package ru.polyarbeiterz.impressionmap.presentation.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.polyarbeiterz.impressionmap.data.service.HostService
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import ru.polyarbeiterz.impressionmap.di.UrlManager
import javax.inject.Inject

@HiltViewModel
class SettingsModel @Inject constructor(
    val hostService: HostService,
    val impressionService: ImpressionService,
    val urlManager: UrlManager,
    application: Application
) : AndroidViewModel(application) {

}