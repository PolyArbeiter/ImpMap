package ru.polyarbeiterz.impressionmap.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.polyarbeiterz.impressionmap.data.dao.ImpressionDao
import ru.polyarbeiterz.impressionmap.data.database.AppDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun getDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "impression_database"
        ).build()
    }

    @Provides
    fun getImpressionDao(
        database: AppDatabase
    ): ImpressionDao {
        return database.impressionDao()
    }

}