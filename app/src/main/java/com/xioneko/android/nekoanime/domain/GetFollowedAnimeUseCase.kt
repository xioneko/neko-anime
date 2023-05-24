package com.xioneko.android.nekoanime.domain

import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.WatchRecord
import com.xioneko.android.nekoanime.domain.model.FollowedAnime
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFollowedAnimeUseCase @Inject constructor(
    userDataRepository: UserDataRepository,
    private val animeRepository: AnimeRepository,
) {
    private val followedIds: Flow<List<Int>> = userDataRepository.followedAnimeIds
    private val watchHistory = userDataRepository.watchHistory


    operator fun invoke(): Flow<List<FollowedAnime>> =
        followedIds.map { idList ->
            idList.mapNotNull { id ->
                animeRepository
                    .getAnimeById(id)
                    .firstOrNull()
            }
        }.combine(watchHistory) { animeList, watchRecords ->
            animeList.map {
                FollowedAnime(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.imageUrl,
                    latestEpisode = it.latestEpisode,
                    currentEpisode = watchRecords[it.id]?.recentEpisode ?: 0,
                    isFinished = checkFinished(it, watchRecords[it.id]),
                )
            }
        }

    private fun checkFinished(anime: Anime, watchRecord: WatchRecord?) =
        anime.status == "已完结" &&
                watchRecord?.progress?.get(anime.latestEpisode)?.let { it > 90 }
                ?: false
}