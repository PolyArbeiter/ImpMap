package ru.polyarbeiterz.impressionmap.presentation.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import javax.inject.Inject

@HiltViewModel
class ImpressionAdditionModel @Inject constructor(
    val impressionService: ImpressionService,
    application: Application
) : AndroidViewModel(application) {

    val allImpressions: StateFlow<List<ImpressionLocal>> = impressionService.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getImpressionById(id: Int): Flow<ImpressionLocal> {
        return impressionService.getById(id)
    }

    fun insertImp(imp: ImpressionLocal) {
        viewModelScope.launch {
            impressionService.insertAll(imp)
        }
    }
    fun updateImpression(impression: ImpressionLocal) {
        viewModelScope.launch {
            impressionService.update(impression)
        }
    }
}