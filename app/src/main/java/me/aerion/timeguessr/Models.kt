package me.aerion.timeguessr

import com.google.android.gms.maps.model.LatLng

data class Guess(val year: Number, val position: LatLng)

data class RoundResult(val yearScore: Int, val distanceScore: Int, val guess : Guess) {
    val totalScore = yearScore + distanceScore
}