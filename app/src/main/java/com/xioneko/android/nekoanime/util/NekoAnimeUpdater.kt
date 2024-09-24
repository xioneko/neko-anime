package com.xioneko.android.nekoanime.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton


private const val BASE_URL = "https://github.com/xioneko/neko-anime/"

@Singleton
class NekoAnimeUpdater @Inject constructor(
    httpClient: OkHttpClient,
    @ApplicationContext val context: Context
) {
    private val updateApi = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(BASE_URL)
        .build()
        .create(UpdateApi::class.java)

    private val _isUpdateAvailable = MutableStateFlow(false)

    val isUpdateAvailable = _isUpdateAvailable.asStateFlow()

    var latestVersion: String? = null

    var updateNotes: String? = null

    suspend fun checkForUpdate() {
        val response = updateApi
            .getLatestRelease()
            .takeIf { it.isSuccessful }
            ?: return

        latestVersion = response
            .raw()
            .request
            .url // 重定向的URL
            .toString()
            .substringAfterLast("/v")

        updateNotes = Jsoup.parse(response.body()!!.string())
            .select(".markdown-body")
            .first()
            ?.html()

        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        _isUpdateAvailable.emit(latestVersion != versionName)
    }

    fun silent() {
        _isUpdateAvailable.value = false
    }

    fun openDownloadLink(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$BASE_URL/releases/latest"))
        context.startActivity(intent)
    }
}

private interface UpdateApi {
    @GET("releases/latest")
    suspend fun getLatestRelease(): Response<ResponseBody>
}