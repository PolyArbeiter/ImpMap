package ru.polyarbeiterz.impressionmap.data.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.stringPreferencesKey

val Context.dataStore by preferencesDataStore("app_preferences")

object PreferencesKeys {
    val SELECTED_HOST_NAME = stringPreferencesKey("selected_host_name")
    val SELECTED_HOST_IP = stringPreferencesKey("selected_host_ip")
    val SELECTED_HOST_PORT = intPreferencesKey("selected_host_port")
    val SELECTED_USERNAME = stringPreferencesKey("selected_username")
    val SELECTED_PASSWORD = stringPreferencesKey("selected_password")
}

data class UserCredentials(
    val username: String,
    val password: String
)

fun UserCredentials.getBasicAuth(): String {
    val bytes = (this.username + ":" + this.password)
        .encodeToByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}