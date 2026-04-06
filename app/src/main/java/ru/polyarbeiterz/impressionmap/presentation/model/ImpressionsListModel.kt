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
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import javax.inject.Inject

@HiltViewModel
class ImpressionsListModel @Inject constructor(
    val impressionService: ImpressionService,
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    val allImpressions: StateFlow<List<ImpressionLocal>> = impressionService.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun insertImp(imp: ImpressionLocal) {
        impressionService.insertAll(imp)
    }
}