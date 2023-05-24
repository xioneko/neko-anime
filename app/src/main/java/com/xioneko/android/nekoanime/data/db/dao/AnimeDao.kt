package com.xioneko.android.nekoanime.data.db.dao

import androidx.room.*
import com.xioneko.android.nekoanime.data.db.model.AnimeEntity

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime WHERE :animeId = id")
    suspend fun findById(animeId: Int): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeEntity)

    @Query("DELETE FROM anime WHERE :animeId = id")
    suspend fun delete(animeId: Int)
}