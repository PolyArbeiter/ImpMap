package ru.polyarbeiterz.impressionmap.data.service

import android.R.attr.id
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import ru.polyarbeiterz.impressionmap.data.dao.ImpressionDao
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpressionService @Inject constructor(
    private val impDao: ImpressionDao
) {
    fun getAll(): Flow<List<ImpressionLocal>> {
        return impDao.getAll()
    }

    fun getById(impressionId: Int): Flow<ImpressionLocal> {
        return impDao.getById(impressionId)
            .catch { e ->
                Log.e("DATABASE", "Ошибка при чтении записи ID=$id", e)
            }
    }

    suspend fun update(impression: ImpressionLocal) {
        impDao.update(impression)
    }

    suspend fun insertAll(vararg impressionLocals: ImpressionLocal) {
        impDao.insertAll(*impressionLocals)
    }
}