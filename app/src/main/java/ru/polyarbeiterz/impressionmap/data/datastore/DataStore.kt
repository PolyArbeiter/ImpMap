package ru.polyarbeiterz.impressionmap.data.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val Context.dataStore by preferencesDataStore("app_preferences")

object PreferencesKeys {
    val SELECTED_HOST_NAME = stringPreferencesKey("selected_host_name")
    val SELECTED_HOST_IP = stringPreferencesKey("selected_host_ip")
    val SELECTED_HOST_PORT = intPreferencesKey("selected_host_port")
    val SELECTED_USERNAME = stringPreferencesKey("selected_username")
    val SELECTED_PASSWORD = stringPreferencesKey("selected_password")
    val PROFILE_IMAGE = byteArrayPreferencesKey("profile_image")
    val PROFILE_USERNAME = stringPreferencesKey("profile_username")
    val PROFILE_EMAIL = stringPreferencesKey("profile_email")
}

data class UserCredentials(
    val username: String,
    val password: String
)

data class UserProfile(
    val image: ByteArray?,
    val username: String,
    val email: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserProfile

        if (!image.contentEquals(other.image)) return false
        if (username != other.username) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image.contentHashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + email.hashCode()
        return result
    }
}

fun UserCredentials.getBasicAuth(): String {
    val bytes = (this.username + ":" + this.password)
        .encodeToByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun UserCredentials.isNotBlank() = this.username.isNotBlank() && this.password.isNotBlank()