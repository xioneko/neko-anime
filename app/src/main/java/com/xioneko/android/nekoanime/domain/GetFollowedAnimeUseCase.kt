package com.xioneko.android.nekoanime.domain

import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.WatchRecord
import com.xioneko.android.nekoanime.domain.model.FollowedAnime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
        followedIds.map { it.reversed() }
            .combine(watchHistory) { idList, watchRecords ->
                emit(idList.map { FollowedAnime(id = it) })
                idList.mapNotNull { id ->
                    animeRepository
                        .getAnimeById(id)
                        .firstOrNull()
                        ?.let {
                            FollowedAnime(
                                id = it.id,
                                name = it.name,
                                imageUrl = it.imageUrl,
                                currentEpisode = watchRecords[it.id]?.recentEpisode ?: 0,
                                isFinished = checkFinished(watchRecords[it.id], it.latestEpisode),
                                lastWatchingDate = watchRecords[it.id]?.date?.timeInMillis ?: 0
                            )
                        }
                }
            }.collect {
                emit(it)
            }
    }

    private fun checkFinished(watchRecord: WatchRecord?, latestEpisode: Int): Boolean {
        if (watchRecord == null) return false
        for (ep in 1..latestEpisode) {
            if (watchRecord.progress[ep] == null
                || watchRecord.progress[ep]!! < 90
            ) return false
        }
        return true
    }
}