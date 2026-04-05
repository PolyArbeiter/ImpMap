package ru.polyarbeiterz.impressionmap.presentation.model

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import javax.inject.Inject

@HiltViewModel
class ImpressionAdditionModel @Inject constructor(
    val impressionService: ImpressionService
) : ViewModel() {
    suspend fun insertImp(imp: ImpressionLocal) {
        impressionService.insertAll(imp)
    }

    suspend fun getAllImpressions(): List<ImpressionLocal> {
        return impressionService.getAll()
    }
}