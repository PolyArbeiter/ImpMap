package ru.polyarbeiterz.impressionmap.data.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey

val Context.dataStore by preferencesDataStore("app_preferences")

object PreferencesKeys {
    val SELECTED_HOST_NAME = stringPreferencesKey("selected_host_name")
    val SELECTED_HOST_IP = stringPreferencesKey("selected_host_ip")
    val SELECTED_HOST_PORT = intPreferencesKey("selected_host_port")
}
