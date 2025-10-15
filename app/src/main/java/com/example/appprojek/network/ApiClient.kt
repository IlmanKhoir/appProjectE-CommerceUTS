package com.example.appprojek.network

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
