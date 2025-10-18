package com.example.appprojek.util

import org.osmdroid.util.GeoPoint

object OsrmUtil {
    // Return a simple interpolated route between input points to simulate OSRM snapping
    fun matchRoute(points: List<GeoPoint>): List<GeoPoint> {
        if (points.size <= 2) return points
        val result = mutableListOf<GeoPoint>()
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            result.add(a)
            // add 5 intermediate points
            val steps = 5
            for (s in 1..steps) {
                val t = s.toDouble() / (steps + 1)
                val lat = a.latitude + (b.latitude - a.latitude) * t
                val lon = a.longitude + (b.longitude - a.longitude) * t
                result.add(GeoPoint(lat, lon))
            }
        }
        // add last
        result.add(points.last())
        return result
    }
}
