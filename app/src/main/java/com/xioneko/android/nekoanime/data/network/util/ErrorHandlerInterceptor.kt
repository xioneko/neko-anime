package com.xioneko.android.nekoanime.data.network.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

object ErrorHandlerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val response = chain.proceed(chain.request())

            return response.newBuilder()
                .body(response.body)
                .build()
        } catch (e: Exception) {
            Log.e("Network", "Connect Error: ${e.message}", e)
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(1)
                .message("connect error")
                .body("{$e}".toResponseBody(null))
                .build()
        }
    }
}