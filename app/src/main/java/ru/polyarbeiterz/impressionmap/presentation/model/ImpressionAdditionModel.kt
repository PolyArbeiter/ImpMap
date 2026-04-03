package ru.polyarbeiterz.impressionmap.presentation.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.entity.Impression
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import javax.inject.Inject

@HiltViewModel
class ImpressionAdditionModel @Inject constructor(
    val impService: ImpressionService
) : ViewModel() {
    fun insertImp(imp: Impression) {
        viewModelScope.launch {
            impService.insertAll(imp)
        }
    }
}