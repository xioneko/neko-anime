package com.xioneko.android.nekoanime.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xioneko.android.nekoanime.data.db.dao.AnimeDao
import com.xioneko.android.nekoanime.data.db.model.AnimeEntity

@Database(entities = [AnimeEntity::class], version = 1)
abstract class AnimeDatabase: RoomDatabase() {
    abstract fun  animeDao(): AnimeDao
}