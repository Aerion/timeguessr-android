package me.aerion.timeguessr

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import me.aerion.timeguessr.ui.theme.TimeguessrTheme
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage


// TODO: Restrict api key

class MainActivity : ComponentActivity() {
    private val roundDataSource: RoundDataFetcher = StubbedRoundDataFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeguessrTheme {
                val rounds = remember { mutableStateOf<List<RoundData>?>(null) }

                LaunchedEffect(Unit) {
                    rounds.value = roundDataSource.fetchRounds()
                }

                // Show a loader while the data is being fetched
                if (rounds.value == null) {
                    Text("Loading...")
                    return@TimeguessrTheme
                }

                RoundPlayPage(
                    roundData = rounds.value!!.first(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
enum class FullscreenState {
    NONE,
    IMAGE,
    MAP
}

@Composable
fun RoundPlayPage(roundData: RoundData, modifier: Modifier = Modifier) {
    val initialLocation = LatLng(51.48633971492552, 3.691691980292835)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 4f)
    }

    var positionGuess by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var markerStateGuess by remember { mutableStateOf<MarkerState?>(null) }
    var fullscreenState by remember { mutableStateOf(FullscreenState.NONE) }

    val distance = FloatArray(1)
    Location.distanceBetween(
        roundData.Location.lat,
        roundData.Location.lng,
        positionGuess.latitude,
        positionGuess.longitude,
        distance
    )
    val distanceInMeters = distance[0]

    BackHandler(enabled = fullscreenState != FullscreenState.NONE) {
        fullscreenState = FullscreenState.NONE
    }

    Column(modifier) {
        if (fullscreenState == FullscreenState.NONE || fullscreenState == FullscreenState.IMAGE) {
            FullscreenContainer(
                modifier = Modifier.fillMaxHeight(0.4f),
                isFullscreen = fullscreenState == FullscreenState.IMAGE,
                onFullscreenToggle = {
                    fullscreenState =
                        if (fullscreenState == FullscreenState.IMAGE) FullscreenState.NONE else FullscreenState.IMAGE
                }
            ) {
                ZoomableAsyncImage(
                    model = roundData.URL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (fullscreenState == FullscreenState.NONE) {
            Text(roundData.Location.lat.toString() + " " + roundData.Location.lng.toString())
            Text(positionGuess.toString())
            Text("$distanceInMeters meters away")
        }

        if (fullscreenState == FullscreenState.NONE || fullscreenState == FullscreenState.MAP) {
            FullscreenContainer(
                modifier = Modifier.fillMaxHeight(0.4f),
                isFullscreen = fullscreenState == FullscreenState.MAP,
                onFullscreenToggle = {
                    fullscreenState =
                        if (fullscreenState == FullscreenState.MAP) FullscreenState.NONE else FullscreenState.MAP
                }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        indoorLevelPickerEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    ),
                    onMapClick = { position ->
                        positionGuess = position
                        markerStateGuess = MarkerState(
                            position = position
                        )
                    }
                ) {
                    // Show the guess from the user
                    if (markerStateGuess != null) {
                        AdvancedMarker(
                            state = markerStateGuess!!,
                        )
                    }
                }
            }
        }

        if (fullscreenState == FullscreenState.NONE) {
            YearInputField()
        }
    }
}

@Composable
fun FullscreenContainer(
    modifier: Modifier = Modifier,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = if (isFullscreen) Modifier.fillMaxSize() else modifier) {
        content()

        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = "Fullscreen",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clickable(onClick = onFullscreenToggle)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun YearInputField() {
    var year by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = year,
        onValueChange = { newValue ->
            // Allow only numeric input and limit to 4 characters
            if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                year = newValue
            }
        },
        label = { Text("Enter Year") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                // Close the keyboard when Enter is pressed
                keyboardController?.hide()
            }
        )
    )
}