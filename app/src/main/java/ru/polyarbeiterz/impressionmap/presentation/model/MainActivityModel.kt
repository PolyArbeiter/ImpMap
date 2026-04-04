package ru.polyarbeiterz.impressionmap.presentation.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.data.service.HostService
import javax.inject.Inject

@HiltViewModel
class MainActivityModel @Inject constructor(
    val hostService: HostService
) : ViewModel() {
    fun insertHost(host: Host) {
        viewModelScope.launch {
            hostService.insertAll(host)
        }
    }
}