package com.example.appprojek.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
                sin(dLat / 2) * sin(dLat / 2) +
                        cos(Math.toRadians(lat1)) *
                                cos(Math.toRadians(lat2)) *
                                sin(dLon / 2) *
                                sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun estimateEtaMinutes(distanceKm: Double, avgSpeedKmh: Double = 25.0): Int {
        if (avgSpeedKmh <= 0.0) return 0
        val hours = distanceKm / avgSpeedKmh
        return (hours * 60).roundToInt().coerceAtLeast(1)
    }
}
