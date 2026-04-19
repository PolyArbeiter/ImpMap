package ru.polyarbeiterz.impressionmap.core.service

import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.polyarbeiterz.impressionmap.core.logic.Impression
import ru.polyarbeiterz.impressionmap.core.logic.Media
import ru.polyarbeiterz.impressionmap.core.logic.toCore
import ru.polyarbeiterz.impressionmap.core.logic.toLocal
import ru.polyarbeiterz.impressionmap.core.utils.formatDateForServer
import ru.polyarbeiterz.impressionmap.data.dto.MediaResponse
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionBackendService
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import ru.polyarbeiterz.impressionmap.data.service.MediaService
import ru.polyarbeiterz.impressionmap.di.UrlManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ImpressionCoreService @Inject constructor(
    private val impressionService: ImpressionService,
    private val impressionBackendService: ImpressionBackendService,
    private val mediaService: MediaService,
    private val urlManager: UrlManager
) {

    suspend fun getLocalAll(withMedia: Boolean = false): List<Impression> {
        val result = impressionService.getAll().map { imp ->
            val mediaLocalList = mediaService
                .getByImpressionId(imp.id)
                .map {media -> media.toCore()}
            imp.toCore(mediaLocalList)
        }
        return result
    }

    suspend fun getRemoteAll(withMedia: Boolean = false): List<Impression> {
        val response = impressionBackendService.getAllImpressions()
        if (!response.isSuccessful) return emptyList()

        val impressionsDto = response.body() ?: return emptyList()

        if (!withMedia) {
            return impressionsDto.map { it.toCore(emptyList()) }
        }

        return impressionsDto.map { impDto ->
            val mediaList = impDto.media.map { mediaDto ->
                downloadMedia(mediaDto)
            }.filterNotNull()

            impDto.toCore(mediaList)
        }
    }

    private suspend fun downloadMedia(media: MediaResponse): Media? {
        return try {
            val response = if (media.isVideo) {
                impressionBackendService.getVideo(
                    media.file.replace("${urlManager.baseUrl.value}/", "")
                )
            } else {
                impressionBackendService.getImage(
                    media.file.replace("${urlManager.baseUrl.value}/", "")
                )
            }

            if (!response.isSuccessful) return null

            val byteArray = response.body() ?: return null
            val mediaType = if (media.isVideo) {
                MediaLocal.MediaType.VIDEO
            } else {
                MediaLocal.MediaType.IMAGE
            }
            val mimeType = if (media.isVideo) "video/mp4" else "image/jpeg"

            media.toCore(
                fileData = byteArray.bytes(),
                mediaType = mediaType,
                mimeType = mimeType,
            )
        } catch (e: Exception) {
            Log.e("MEDIA", "Failed to download: ${e.message}")
            null
        }
    }

    suspend fun createImpressionWithMediaRemote(impression: Impression) {
        impressionBackendService.createImpression(
            localId = RequestBody.create(MediaType.parse("text/plain"), impression.localId.toString()),
            title = RequestBody.create(MediaType.parse("text/plain"), impression.title),
            description = RequestBody.create(MediaType.parse("text/plain"), impression.description),
            latitude = RequestBody.create(MediaType.parse("text/plain"), impression.latitude.toString()),
            longitude = RequestBody.create(MediaType.parse("text/plain"), impression.longitude.toString()),
            date = RequestBody.create(MediaType.parse("text/plain"), formatDateForServer(impression.date)),
            media = impression.media.mapIndexed { index, it ->
                toMultiPartFile(byteArray = it.fileData, mediaType = it.mediaType, index = index)
            }
        )
    }
    fun toMultiPartFile(
        name: String = "media",
        byteArray: ByteArray,
        mediaType: MediaLocal.MediaType,
        index: Int
    ): MultipartBody.Part {
        val mimeType = if(mediaType == MediaLocal.MediaType.IMAGE) {
            "image/jpeg"
        } else { "video/mp4" }

        val reqFile = RequestBody.create(MediaType.parse(
            mimeType
        ), byteArray)

        return MultipartBody.Part.createFormData(
            name,
            "file_$index.jpg",
            reqFile
        )
    }

    suspend fun createImpressionWithMediaLocal(impression: Impression) {
        impressionService.insert(
            impression.toLocal()
        )
        impression.media.forEach { media ->
            val mediaLocal = media.toLocal(impression.localId)
            if (mediaLocal.mediaType == MediaLocal.MediaType.IMAGE)
                mediaService.insert(mediaLocal)
        }
    }
}