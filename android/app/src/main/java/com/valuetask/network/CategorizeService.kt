package com.valuetask.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CategorizeService {

    /**
     * ⚠️  Replace YOUR_VERCEL_URL with your actual Vercel project URL.
     *
     * After deploying, find your URL in:
     *   Vercel Dashboard → your project → Deployments → the URL shown
     *
     * Example:
     *   https://valuetask.vercel.app/api/categorizeTodo
     */
    private const val FUNCTION_URL =
        "https://YOUR_VERCEL_URL.vercel.app/api/categorizeTodo"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Calls the Cloud Function and returns the category string.
     * Throws on network error or non-2xx HTTP status.
     * Always runs on [Dispatchers.IO].
     */
    suspend fun categorize(text: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject().put("text", text)
            .toString()
            .toRequestBody(JSON_TYPE)

        val request = Request.Builder()
            .url(FUNCTION_URL)
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Server error ${response.code}")
        }

        val responseText = response.body?.string()
            ?: throw Exception("Empty response body")

        JSONObject(responseText).getString("category")
    }
}
