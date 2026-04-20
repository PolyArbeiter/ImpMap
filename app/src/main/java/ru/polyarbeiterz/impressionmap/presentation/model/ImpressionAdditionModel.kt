package ru.polyarbeiterz.impressionmap.presentation.model

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import ru.polyarbeiterz.impressionmap.data.service.MediaService
import javax.inject.Inject
import ru.polyarbeiterz.impressionmap.core.utils.compressImageToByteArray

@HiltViewModel
class ImpressionAdditionModel @Inject constructor(
    val mediaService: MediaService,
    val impressionService: ImpressionService,
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    val allImpressions: StateFlow<List<ImpressionLocal>> = impressionService.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getImpById(id: Int): Flow<ImpressionLocal> {
        return impressionService.getByImpId(id)
    }

    suspend fun insertImp(imp: ImpressionLocal): Long {
        return impressionService.insert(imp)
    }

    fun updateImp(impression: ImpressionLocal) {
        viewModelScope.launch {
            impressionService.update(impression)
        }
    }

    fun deleteImp(impressionId: Int) {
        viewModelScope.launch {
            impressionService.delete(impressionId)
        }
    }

    fun addMediaToImpression(uri: Uri, type: MediaLocal.MediaType, impressionId: Int) {
        viewModelScope.launch {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            val fileData = if (type == MediaLocal.MediaType.IMAGE) {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    compressImageToByteArray(bitmap, quality = 20, maxWidth = 200, maxHeight = 200)
                } else {
                    return@launch
                }
            } else {
                context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
            }

            val mediaItem = MediaLocal(
                impressionId = impressionId,
                fileData = fileData,
                mediaType = type,
                mimeType = mimeType
            )
            mediaService.insert(mediaItem)
        }
    }

    fun getMediaByImpId(impressionId: Int): Flow<List<MediaLocal>> {
        return mediaService.getByImpressionIdFlow(impressionId)
    }

    fun deleteMedia(mediaItem: MediaLocal) {
        viewModelScope.launch {
            mediaService.delete(mediaItem)
        }
    }
}