package com.xioneko.android.nekoanime.domain

import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.domain.model.FollowedAnime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFollowedAnimeUseCase @Inject constructor(
    userDataRepository: UserDataRepository,
    private val animeRepository: AnimeRepository,
) {
    private val followedIds: Flow<List<Int>> = userDataRepository.followedAnimeIds
    private val watchHistory = userDataRepository.watchHistory


    operator fun invoke(): Flow<List<FollowedAnime>> = flow {
        emitAll(
            followedIds.take(1).map { idList ->
                idList.map { FollowedAnime(id = it) }
            }
        )

        emitAll(
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
                        currentEpisode = watchRecords[it.id]?.recentEpisode ?: 0,
                        isFinished = (watchRecords[it.id]?.progress?.get(it.latestEpisode)
                            ?: 0) > 90,
                    )
                }
            }
        )
    }
}