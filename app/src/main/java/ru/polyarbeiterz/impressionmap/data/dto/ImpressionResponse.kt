package ru.polyarbeiterz.impressionmap.data.dto
data class ImpressionResponse(
    //    val userId: Long? = null,
    //    val timeCreated: Long? = null,
    //    val timeModified: Long? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val date: String? = null,
    val title: String? = null,
    val description: String? = null,
    val onServer: Boolean = true,
    val media: List<MediaDto> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImpressionResponse

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (date != other.date) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (onServer != other.onServer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude?.hashCode() ?: 0
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + onServer.hashCode()
        return result
    }
}

