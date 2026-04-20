package ru.polyarbeiterz.impressionmap.presentation.model

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.datastore.PreferencesKeys
import ru.polyarbeiterz.impressionmap.data.datastore.UserCredentials
import ru.polyarbeiterz.impressionmap.data.datastore.UserProfile
import ru.polyarbeiterz.impressionmap.data.datastore.dataStore
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
    private val context = getApplication<Application>()

    val selectedUserProfile: Flow<UserProfile> = context.dataStore.data
        .map { preferences ->
            val image = preferences[PreferencesKeys.PROFILE_IMAGE]
            val username = preferences[PreferencesKeys.PROFILE_USERNAME] ?: "Имя"
            val email = preferences[PreferencesKeys.PROFILE_EMAIL] ?: "Почта"
            UserProfile(image, username, email)
        }

    fun saveUserProfile(
        username: String,
        email: String,
        profileImage: ByteArray?
    ) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                profileImage?.let {
                    preferences[PreferencesKeys.PROFILE_IMAGE] = it
                }
                preferences[PreferencesKeys.PROFILE_USERNAME] = username
                preferences[PreferencesKeys.PROFILE_EMAIL] = email
            }
        }
    }
}