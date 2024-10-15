package me.aerion.timeguessr

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Guess(val year: Number, val position: LatLng) : Parcelable

@Parcelize
data class RoundResult(val yearScore: Int, val distanceScore: Int, val distanceDiffInMeters: Int, val guess : Guess) : Parcelable {
    @IgnoredOnParcel
    val totalScore = yearScore + distanceScore
}