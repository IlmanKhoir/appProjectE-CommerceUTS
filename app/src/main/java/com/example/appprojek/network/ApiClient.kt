package com.example.appprojek.network

/*
 * ApiClient.kt
 *
 * NOTE: This file provides a lightweight OkHttp wrapper used by the app when
 * connecting to a real backend. The project has been converted to a MOCK mode
 * for UI/demo purposes: repositories now use an in-memory DummyDatabase and
 * shipping/tracking uses local simulation. As a result, `ApiClient` is currently
 * unused by the main app flows.
 *
 * Recommended (safe) workflow:
 * 1) Keep this file in the repository as a fallback to re-enable network mode.
 * 2) If you want to mark it clearly as legacy, you can rename it to
 *    `ApiClient.legacy.kt` or move it to a `legacy/` package. Avoid deleting it
 *    immediately unless you are sure network mode is not needed.
 */

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiClient {
    // Set this to your PHP server base URL, without trailing slash
    // Example: http://192.168.1.10/appprojek
    var BASE_URL: String = "http://192.168.1.19/appprojek"

    val httpClient: OkHttpClient by lazy { OkHttpClient() }

    fun buildGet(pathAndQuery: String): Request =
            Request.Builder().url("$BASE_URL/$pathAndQuery").get().build()

    fun buildPostForm(path: String, form: Map<String, String>): Request {
        val formBuilder = FormBody.Builder()
        form.forEach { (k, v) -> formBuilder.add(k, v) }
        return Request.Builder()
                .url("$BASE_URL/$path")
                .addHeader("Accept", "application/json")
                .post(formBuilder.build())
                .build()
    }
}
