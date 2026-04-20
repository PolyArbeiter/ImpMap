package ru.polyarbeiterz.impressionmap.presentation.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.datastore.PreferencesKeys
import ru.polyarbeiterz.impressionmap.data.datastore.UserProfile
import ru.polyarbeiterz.impressionmap.data.datastore.dataStore
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import javax.inject.Inject

@HiltViewModel
class ImpressionsListModel @Inject constructor(
    val impressionService: ImpressionService,
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    val allImpressions: StateFlow<List<ImpressionLocal>> = impressionService.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertImp(imp: ImpressionLocal) {
        viewModelScope.launch {
            impressionService.insertAll(imp)
        }
    }
    fun deleteImp(impressionId: Int) {
        viewModelScope.launch {
            impressionService.delete(impressionId)
        }
    }

    val selectedUserProfile: Flow<UserProfile> = context.dataStore.data
        .map { preferences ->
            val image = preferences[PreferencesKeys.PROFILE_IMAGE]
            val username = preferences[PreferencesKeys.PROFILE_USERNAME] ?: "Имя"
            val email = preferences[PreferencesKeys.PROFILE_EMAIL] ?: "Почта"
            UserProfile(image, username, email)
        }
}