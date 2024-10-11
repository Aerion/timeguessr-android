package me.aerion.timeguessr

import android.annotation.SuppressLint
import java.text.NumberFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@SuppressLint("UnrememberedMutableState")
@Composable
fun RoundResultPage(
    round : RoundData,
    roundResult : RoundResult,
    isLastRound: Boolean,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalScoreString = NumberFormat.getInstance().format(roundResult.totalScore)
    val yearScoreString = NumberFormat.getInstance().format(roundResult.yearScore)
    val distanceScoreString = NumberFormat.getInstance().format(roundResult.distanceScore)
    val yearDifference = kotlin.math.abs(roundResult.guess.year.toInt() - round.Year.toInt())
    val resultLocation = round.Location.toLatLng()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(resultLocation, 1f)
    }

    LaunchedEffect(key1 = true ){
        val boundsBuilder = LatLngBounds.builder()
        boundsBuilder.include(resultLocation)
        boundsBuilder.include(roundResult.guess.position)
        val bounds = boundsBuilder.build()

        cameraPositionState.move(
            update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
        )
    }

    Column(modifier = modifier) {
        Text(round.Description)
        Text("${round.Year} you were $yearDifference years off")

        Text("Year Score: $yearScoreString/5000")
        Text("Distance Score: $distanceScoreString/5000")
        Text("Total Score: $totalScoreString/10000")
        Spacer(modifier = Modifier.height(16.dp))
        ZoomableAsyncImage(
            model = round.URL,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GoogleMap(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            cameraPositionState = cameraPositionState
        ) {
            AdvancedMarker(state = MarkerState(position = resultLocation))
            AdvancedMarker(state = MarkerState(position = roundResult.guess.position))
        }

        Button(
            onClick = onNextRound
        ) {
            Text(if (isLastRound) "Show results" else "Next Round")
        }
    }
}

private class BooleanProvider: PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(
        true, false
    )
    override val count: Int = values.count()
}

@Preview(showBackground = true)
@Composable
fun RoundResultPagePreview(@PreviewParameter(BooleanProvider::class) isLastRound: Boolean) {
    Surface(modifier = Modifier.fillMaxSize()) {
        RoundResultPage(
            round = RoundData(
                Description = "A description",
                Year = "2021",
                Location = Location(51.48633971492552, 3.691691980292835),
                URL = "https://example.com/image.jpg",
                No = "123",
                Country = "Netherlands",
                License = "License"
            ),
            roundResult = RoundResult(
                yearScore = 1234,
                distanceScore = 5678,
                guess = Guess(2020, LatLng(51.48633971492552, 3.691691980292835))
            ),
            isLastRound = isLastRound,
            onNextRound = {}
        )
    }
}