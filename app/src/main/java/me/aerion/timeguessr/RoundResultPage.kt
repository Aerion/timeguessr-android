package me.aerion.timeguessr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import java.text.NumberFormat

@Composable
fun RoundResultPage(
    round: RoundData,
    roundResult: RoundResult,
    isLastRound: Boolean,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalScoreString = NumberFormat.getInstance().format(roundResult.totalScore)
    val yearScoreString = NumberFormat.getInstance().format(roundResult.yearScore)
    val distanceScoreString = NumberFormat.getInstance().format(roundResult.distanceScore)
    val maxScoreString = NumberFormat.getInstance().format(10000)
    val subMaxScoreString = NumberFormat.getInstance().format(5000)
    val yearDifference = kotlin.math.abs(roundResult.guess.year.toInt() - round.Year.toInt())
    val resultLocation = round.Location.toLatLng()
    val distanceDiffWithUnitString = if (roundResult.distanceDiffInMeters < 1000) {
        "${roundResult.distanceDiffInMeters} meters"
    } else {
        "${roundResult.distanceDiffInMeters / 1000} km"
    }

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < maxHeight) {
            // Portrait layout
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RoundDescriptionCard(
                    round = round,
                    yearDifference = yearDifference,
                    yearScoreString = yearScoreString,
                    subMaxScoreString = subMaxScoreString,
                    modifier = Modifier.weight(1f)
                )
                MapDistanceCard(
                    resultLocation = resultLocation,
                    roundResult = roundResult,
                    distanceDiffWithUnitString = distanceDiffWithUnitString,
                    distanceScoreString = distanceScoreString,
                    subMaxScoreString = subMaxScoreString,
                    modifier = Modifier.weight(1f)
                )
                RoundScoreCard(
                    totalScoreString = totalScoreString,
                    maxScoreString = maxScoreString,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = onNextRound,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (isLastRound) "Show results" else "Next Round")
                }
            }
        } else {
            // Landscape layout
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoundDescriptionCard(
                        round = round,
                        yearDifference = yearDifference,
                        yearScoreString = yearScoreString,
                        subMaxScoreString = subMaxScoreString,
                        modifier = Modifier.weight(1f)
                    )
                    MapDistanceCard(
                        resultLocation = resultLocation,
                        roundResult = roundResult,
                        distanceDiffWithUnitString = distanceDiffWithUnitString,
                        distanceScoreString = distanceScoreString,
                        subMaxScoreString = subMaxScoreString,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundScoreCard(
                        totalScoreString = totalScoreString,
                        maxScoreString = maxScoreString,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onNextRound
                    ) {
                        Text(if (isLastRound) "Show results" else "Next Round")
                    }
                }
            }
        }
    }
}

@Composable
fun RoundDescriptionCard(round: RoundData, yearDifference: Int, yearScoreString: String, subMaxScoreString: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp).fillMaxWidth()
        ) {
            Text(text = round.Description, fontStyle = FontStyle.Italic)
            ZoomableAsyncImage(
                model = round.URL,
                contentDescription = null,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            )
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(round.Year)
                    }
                }
            )
            Text(
                buildAnnotatedString {
                    append("You were ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(yearDifference.toString())
                    }
                    append(" year")
                    if (yearDifference != 1) {
                        append("s")
                    }
                    append(" off")
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                buildAnnotatedString {
                    append("\uD83D\uDCC5 ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(yearScoreString)
                    }
                    append(" / $subMaxScoreString")
                }
            )
        }
    }
}

@Composable
fun MapDistanceCard(resultLocation: LatLng, roundResult: RoundResult, distanceDiffWithUnitString: String, distanceScoreString: String, subMaxScoreString: String, modifier: Modifier = Modifier) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(resultLocation, 1f)
    }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                minZoomPreference = 1f,
            )
        )
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

    Card(
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp).fillMaxWidth()
        ) {
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    indoorLevelPickerEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                ),
                properties = mapProperties
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

            Text(
                buildAnnotatedString {
                    append("You were ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(distanceDiffWithUnitString)
                    }
                    append(" away")
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                buildAnnotatedString {
                    append("\uD83C\uDF0E ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(distanceScoreString)
                    }
                    append(" / $subMaxScoreString")
                }
            )
        }
    }
}

@Composable
fun RoundScoreCard(totalScoreString: String, maxScoreString: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            Text(
                buildAnnotatedString {
                    append("Round Score: ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(totalScoreString)
                    }
                    append(" / $maxScoreString")
                }
            )
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
                yearScore = 123,
                distanceScore = 5000,
                distanceDiffInMeters = 132,
                guess = Guess(2020, LatLng(51.48633971492552, 3.691691980292835))
            ),
            isLastRound = isLastRound,
            onNextRound = {}
        )
    }
}