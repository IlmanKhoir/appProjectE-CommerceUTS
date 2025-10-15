package com.example.appprojek.util

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

object OsrmUtil {
    private val client = OkHttpClient()
    private const val OSRM_URL = "https://router.project-osrm.org/match/v1/driving/"

    fun matchRoute(points: List<GeoPoint>): List<GeoPoint> {
        val coordString = points.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "$OSRM_URL$coordString?geometries=geojson"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val geometry = json
                .getJSONArray("matchings")
                .optJSONObject(0)
                ?.getJSONObject("geometry")
                ?.getJSONArray("coordinates") ?: return emptyList()
            return (0 until geometry.length()).map { i ->
                val arr = geometry.getJSONArray(i)
                GeoPoint(arr.getDouble(1), arr.getDouble(0))
            }
        }
    }
}
