package com.xioneko.android.nekoanime.data

import com.xioneko.android.nekoanime.data.db.dao.AnimeDao
import com.xioneko.android.nekoanime.data.db.model.asAnime
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeKey
import com.xioneko.android.nekoanime.data.model.asEntity
import kotlinx.coroutines.flow.*
import org.mobilenativefoundation.store.store5.SourceOfTruth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeSourceOfTruth @Inject constructor(
    private val dao: AnimeDao,
) {
    operator fun invoke(): SourceOfTruth<AnimeKey, Anime> = SourceOfTruth.of(
        reader = { key: AnimeKey ->
            flow {
                when (key) {
                    is AnimeKey.FetchAnime -> {
                        emit(dao.findById(key.animeId)?.asAnime())
                    }

                    is AnimeKey.FetchVideo -> {
                        dao.findById(key.anime.id)
                            ?.takeIf { it.videoSource[key.episode] != null }
                            ?.let { emit(it.asAnime()) }
                            ?: emit(null)
                    }
                }
            }
        },
        writer = { _, anime ->
            dao.insert(anime.asEntity())
        },
        delete = { key ->
            when (key) {
                is AnimeKey.FetchAnime -> dao.delete(key.animeId)
                is AnimeKey.FetchVideo -> dao.delete(key.anime.id)
            }
        }
    )
}