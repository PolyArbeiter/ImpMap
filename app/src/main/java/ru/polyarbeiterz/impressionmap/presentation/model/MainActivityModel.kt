package ru.polyarbeiterz.impressionmap.presentation.model

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.polyarbeiterz.impressionmap.data.datastore.PreferencesKeys
import ru.polyarbeiterz.impressionmap.data.datastore.dataStore
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.data.service.HostService
import ru.polyarbeiterz.impressionmap.di.UrlManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityModel @Inject constructor(
    val hostService: HostService,
    val urlManager: UrlManager,
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()
    val allHosts: StateFlow<List<Host>> = hostService.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedHost: Flow<Host?> = context.dataStore.data
        .map { preferences ->
            val name = preferences[PreferencesKeys.SELECTED_HOST_NAME]
            val ip = preferences[PreferencesKeys.SELECTED_HOST_IP]
            val port = preferences[PreferencesKeys.SELECTED_HOST_PORT]

            if (ip == null || port == null) {
                null
            } else {
                Host(name = name, ip = ip, port = port)
            }
        }

    fun selectHost(host: Host) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SELECTED_HOST_NAME] = host.name ?: ""
                preferences[PreferencesKeys.SELECTED_HOST_IP] = host.ip ?: ""
                preferences[PreferencesKeys.SELECTED_HOST_PORT] = host.port ?: -1
            }
        }
    }

    fun selectLocalMode() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SELECTED_HOST_IP] = "127.0.0.1"
                preferences[PreferencesKeys.SELECTED_HOST_PORT] = -1
            }
        }
    }

    fun insertHost(host: Host): Boolean {

        if (host.ip.isNullOrBlank() || host.port == null) {
            return false
        }

        if (!isValidIp(host.ip)) return false
        if (!isValidPort(host.port)) return false

        viewModelScope.launch {
            hostService.insertAll(host)
        }
        return true
    }

    fun isValidIp(ip: String): Boolean {
        val ipPattern = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
        return ipPattern.matches(ip) &&
                ip.split(".").all { it.toIntOrNull() in 0..255 }
    }

    fun isValidPort(port: Int): Boolean {
        return port in 1..65535
    }

    fun deleteHost(host: Host) {
        viewModelScope.launch {
            hostService.deleteAll(host)
            if (host.name == selectedHost.firstOrNull()?.name &&
                host.ip == selectedHost.firstOrNull()?.ip &&
                host.port == selectedHost.firstOrNull()?.port
            )
                selectLocalMode()
        }
    }

    suspend fun checkServerConnection(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url(url + "/api/v1/impressions/impressions/")
                    .head()
                    .build()
                val response = client.newCall(request).execute()
                response.code() == 403
            } catch (e: Exception) {
                Log.e("PING_SERVER", e.message.toString())
                false
            }
        }
    }
}