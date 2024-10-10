package me.aerion.timeguessr

import android.location.Location

fun computeYearScore(year1: Number, year2: Number): Int {
    val yearsOff = kotlin.math.abs(year1.toInt() - year2.toInt())
    return when (yearsOff) {
        0 -> 5000
        1 -> 4950
        2 -> 4800
        3 -> 4600
        4 -> 4300
        5 -> 3900
        6, 7 -> 3400
        in 8..10 -> 2500
        in 11..15 -> 2000
        in 16..20 -> 1000
        else -> 0
    }
}

fun computeDistanceScore(
    latitude1: Double,
    longitude1: Double,
    latitude2: Double,
    longitude2: Double
): Int {
    val distance = FloatArray(1)
    Location.distanceBetween(
        latitude1,
        longitude1,
        latitude2,
        longitude2,
        distance
    )

    val distanceInMeters = distance[0]

    return when {
        distanceInMeters <= 50 -> 5000
        distanceInMeters <= 1000 -> 5000 - (distanceInMeters * 0.02).toInt()
        distanceInMeters <= 5000 -> 4980 - (distanceInMeters * 0.016).toInt()
        distanceInMeters <= 100000 -> 4900 - (distanceInMeters * 0.004).toInt()
        distanceInMeters <= 1000000 -> 4500 - (distanceInMeters * 0.001).toInt()
        distanceInMeters <= 2000000 -> 3500 - (distanceInMeters * 0.0005).toInt()
        distanceInMeters <= 3000000 -> 2500 - (distanceInMeters * 0.00033333).toInt()
        distanceInMeters <= 6000000 -> 1500 - (distanceInMeters * 0.0002).toInt()
        else -> 12
    }
}