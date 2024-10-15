package me.aerion.timeguessr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import java.text.NumberFormat

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

    val distanceDiffWithUnitString = if (roundResult.distanceDiffInMeters < 1000) {
        "${roundResult.distanceDiffInMeters} meters"
    } else {
        "${roundResult.distanceDiffInMeters / 1000} km"
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(resultLocation, 1f)
    }

    val markerStateResult = rememberMarkerState(position = resultLocation)
    val markerStateGuess = rememberMarkerState(position = roundResult.guess.position)

    LaunchedEffect(key1 = true) {
        val boundsBuilder = LatLngBounds.builder()
        boundsBuilder.include(resultLocation)
        boundsBuilder.include(roundResult.guess.position)
        val bounds = boundsBuilder.build()

        cameraPositionState.move(
            update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp),
        ) {
            Text(text = round.Description, fontStyle = FontStyle.Italic)
            ZoomableAsyncImage(
                model = round.URL,
                contentDescription = null,
                modifier = Modifier.weight(1f)
            )
            Text("${round.Year} you were $yearDifference years off")
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp)
        ) {
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    indoorLevelPickerEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                ),
            ) {
                Polyline(
                    points = listOf(resultLocation, roundResult.guess.position),
                    width = 4f,
                    pattern = listOf(Dash(20f), Gap(8f)),
                    color = Color.DarkGray
                )
                AdvancedMarker(state = markerStateResult, title = "Actual location")
                AdvancedMarker(
                    state = markerStateGuess, alpha = .5f,
                    title = "Your guess"
                )
            }

            Text("You were $distanceDiffWithUnitString away")
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text("Year Score: $yearScoreString/5000")
            Text("Distance Score: $distanceScoreString/5000")
            Text("Total Score: $totalScoreString/10000")
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
@Preview(
    "Landscape", showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
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
                distanceDiffInMeters = 132,
                guess = Guess(2020, LatLng(51.48633971492552, 3.691691980292835))
            ),
            isLastRound = isLastRound,
            onNextRound = {}
        )
    }
}