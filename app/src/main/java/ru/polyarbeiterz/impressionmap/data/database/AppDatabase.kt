package ru.polyarbeiterz.impressionmap.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.polyarbeiterz.impressionmap.data.dao.ImpressionDao
import ru.polyarbeiterz.impressionmap.data.entity.Impression

@Database(entities = [Impression::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun impressionDao(): ImpressionDao
}