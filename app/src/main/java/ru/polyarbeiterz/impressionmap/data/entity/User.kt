package ru.polyarbeiterz.impressionmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class User(
    @ColumnInfo(name = "user_id") val userId: Int? = null,
    @ColumnInfo(name = "user_name") val userName: String? = null,
    @ColumnInfo(name = "user_email") val userEmail: String? = null,
    @ColumnInfo(name = "user_image") val userImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (userId != other.userId) return false
        if (userName != other.userName) return false
        if (userEmail != other.userEmail) return false
        if (!userImage.contentEquals(other.userImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId ?: 0
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (userEmail?.hashCode() ?: 0)
        result = 31 * result + (userImage?.contentHashCode() ?: 0)
        return result
    }
}