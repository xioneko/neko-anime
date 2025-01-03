package com.xioneko.android.nekoanime.data.network.danmu

import com.xioneko.android.nekoanime.data.network.danmu.DandanplayDanmuProvider.Companion.ID
import com.xioneko.android.nekoanime.data.network.danmu.api.DanmuSession
import com.xioneko.android.nekoanime.data.network.danmu.api.TimeBasedDanmukuSession
import com.xioneko.android.nekoanime.data.network.danmu.dto.toDanmuku
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

interface DanmuProvider : AutoCloseable {
    val id: String

    //获取弹幕会话
    suspend fun fetch(animeName: String, episodeName: String?): DanmuSession?
}

interface DanmukuProviderFactory {
    val id: String
    fun create(): DanmuProvider
}


@Singleton
class DandanplayDanmuProvider @Inject constructor(
    private val client: OkHttpClient
) : DanmuProvider {
    companion object {
        const val ID = "弹弹Play"
    }

    override val id: String
        get() = ID

    private val dandanClient = DanmuPlayClient(client)
    private val moviewPattern = Regex("全集|HD|正片")
    private val noDigitRegex = Regex("\\D")

    override suspend fun fetch(animeName: String, episodeName: String?): DanmuSession? {
        if (episodeName.isNullOrBlank()) {
            return null
        }
        val formmatedEpisodeName = episodeName.let { name ->
            when {
                moviewPattern.containsMatchIn(name) -> "movie"
                name.contains("第") -> name.replace(noDigitRegex, "")
                else -> name.padStart(2, '0')
            }
        }
        val searchEpisodeResponse = dandanClient.searchEpisode(
            animeName, formmatedEpisodeName
        )
        if (searchEpisodeResponse == null) {
            return null
        }
        if (!searchEpisodeResponse.success || searchEpisodeResponse.animes.isEmpty()) {
            return null
        }
        val firstAnime = searchEpisodeResponse.animes[0]
        val episodes = firstAnime.episodes
        if (episodes.isEmpty()) {
            return null
        }
        val firstEpisode = episodes[0]
        //动画集数ID
        val episodeId = firstEpisode.episodeId.toLong()
        return createSession(episodeId)
    }

    private suspend fun createSession(episodeId: Long): DanmuSession? {
        val list = dandanClient.getDanmuList(episodeId)
        return TimeBasedDanmukuSession.create(
            list.asSequence().mapNotNull { it.toDanmuku() },
            Dispatchers.Default
        )

    }

    override fun close() {
    }
}

class DandanPlayProviderFactory @Inject constructor(
    private val client: OkHttpClient
) : DanmukuProviderFactory {
    override val id: String
        get() = ID

    override fun create(): DanmuProvider {
        return DandanplayDanmuProvider(client)
    }
}
