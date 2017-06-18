/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package cz.josefadamcik.trackontrakt.data.api

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber


/**
 * Use as application interceptor for OkHttp. Will log cache hit/miss info and add x-okhttp-cache header.
 * If you want to see header in result from HttpLoggingInterceptor you must add this interceptor after HttpLoggingInterceptor.
 */
class OkHttpCacheLoggingInterceptor : Interceptor {
    companion object {
        const val HEADER = "x-okhttp-cache"
        /**
         * Cache miss (no response cached)
         */
        const val CACHE_MISS = "miss"
        /**
         * Cache hit - no network request was made.
         */
        const val CACHE_HIT = "hit"
        /**
         * Network request was made but response was "304 not modified" co cached result was returned.
         */
        const val CACHE_VALIDATED_HIT = "validated-hit"
        /**
         * Network request was made and response was 200 with body, so existing cached result was ignored.
         */
        const val CACHE_VALIDATED_MISS = "validated-miss"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response: Response = chain.proceed(request)

        val networkResponse = response.networkResponse()
        val cacheResponse = response.cacheResponse()

        var cacheInfo: String? = null
        if (cacheResponse != null && networkResponse == null) {
            Timber.d("CACHE-HIT: %s %s", request.method(), request.url())
            cacheInfo = CACHE_HIT
        } else if (cacheResponse == null && networkResponse != null) {
            Timber.d("CACHE-MISS: : %s %s", request.method(), request.url())
            cacheInfo = CACHE_MISS
        } else if (cacheResponse.code() == 200 && networkResponse.code() == 304) {
            cacheInfo = CACHE_VALIDATED_HIT
            Timber.d("CACHE-VALIDATED-HIT: : %s %s", request.method(), request.url())
        } else if (cacheResponse.code() == 200 && networkResponse.code() == 200) {
            cacheInfo = CACHE_VALIDATED_MISS
            Timber.d("CACHE-VALIDATED-MISS: : %s %s", request.method(), request.url())
        } else {
            cacheInfo = String.format("cache: %s network: %s", cacheResponse.code(), networkResponse.code())
            Timber.d("CACHE: %s %s, cache: %s, network: %s", request.method(), request.url(), cacheResponse.code(), networkResponse.code())
        }

        return response.newBuilder()
            .addHeader(HEADER, cacheInfo)
            .build()
    }
}