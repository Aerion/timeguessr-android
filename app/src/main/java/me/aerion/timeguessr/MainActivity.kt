package me.aerion.timeguessr

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
                    rounds = rounds.value!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PhotoPage(roundData: RoundData, modifier: Modifier = Modifier) {
    ZoomableAsyncImage(
        model = roundData.URL,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun MapPage(
    roundData: RoundData,
    positionGuess: LatLng,
    onPositionGuessChange: (LatLng) -> Unit,
    markerStateGuess: MarkerState?,
    onMarkerStateGuessChange: (MarkerState?) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialLocation = LatLng(51.48633971492552, 3.691691980292835)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 4f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            indoorLevelPickerEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        onMapClick = { position ->
            onPositionGuessChange(position)
            onMarkerStateGuessChange(MarkerState(position = position))
        }
    ) {
        // Show the guess from the user
        if (markerStateGuess != null) {
            AdvancedMarker(
                state = markerStateGuess,
            )
        }
    }
}

@Composable
fun RoundPlayPage(rounds: List<RoundData>, modifier: Modifier = Modifier) {
    var positionGuess by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var markerStateGuess by remember { mutableStateOf<MarkerState?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var totalScore by remember { mutableIntStateOf(0) }
    var currentRoundIndex by remember { mutableIntStateOf(0) }
    var showModal by remember { mutableStateOf(false) }
    var year by remember { mutableStateOf("") }

    val currentRound = rounds[currentRoundIndex]

    Column(modifier.fillMaxSize()) {
        // Display current score and progress
        Text(
            text = "Score: $totalScore | Round: ${currentRoundIndex + 1}/${rounds.size}",
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> PhotoPage(currentRound, modifier)
                1 -> MapPage(
                    roundData = currentRound,
                    positionGuess = positionGuess,
                    onPositionGuessChange = { positionGuess = it },
                    markerStateGuess = markerStateGuess,
                    onMarkerStateGuessChange = { markerStateGuess = it },
                    modifier = modifier
                )
                2 -> GuessPage(
                    roundData = currentRound,
                    positionGuess = positionGuess,
                    year = year,
                    onYearChange = { year = it },
                    onSubmitGuess = { score ->
                        totalScore += score
                        if (currentRoundIndex < rounds.size - 1) {
                            currentRoundIndex++
                            selectedTabIndex = 0
                            positionGuess = LatLng(0.0, 0.0)
                            year = ""
                            markerStateGuess = null
                            year = ""
                        } else {
                            showModal = true
                        }
                    },
                    modifier = modifier
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                )
            }
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(imageVector = Icons.Default.Face, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Photo")
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(imageVector = Icons.Default.Place, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Map")
            }
            Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(imageVector = Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Guess")
            }
        }
    }

    if (showModal) {
        AlertDialog(
            onDismissRequest = { showModal = false },
            title = { Text("Total Score") },
            text = { Text("Your total score is $totalScore") },
            confirmButton = {
                Button(onClick = { showModal = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun GuessPage(
    roundData: RoundData,
    positionGuess: LatLng,
    year: String,
    onYearChange: (String) -> Unit,
    onSubmitGuess: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSubmitEnabled = year.isNotEmpty() && positionGuess.latitude != 0.0 && positionGuess.longitude != 0.0
    var showModal by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var yearScore by remember { mutableIntStateOf(0) }
    var distanceScore by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        TextField(
            value = year,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                    onYearChange(newValue)
                }
            },
            label = { Text("Enter Year") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )
        Button(onClick = {
            val distance = FloatArray(1)
            Location.distanceBetween(
                roundData.Location.lat,
                roundData.Location.lng,
                positionGuess.latitude,
                positionGuess.longitude,
                distance
            )
            yearScore = computeYearScore(year, roundData.Year)
            distanceScore = computeDistanceScore(distance[0])
            score = yearScore + distanceScore
            showModal = true
        },
            enabled = isSubmitEnabled
        ) {
            Text("Submit Guess")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.Done, contentDescription = null)
        }
    }

    ResultModal(
        showModal = showModal,
        onDismiss = {
            showModal = false
            onSubmitGuess(score)
        },
        roundData = roundData,
        positionGuess = positionGuess,
        score = score,
        yearScore = yearScore,
        distanceScore = distanceScore
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ResultModal(
    showModal: Boolean,
    onDismiss: () -> Unit,
    roundData: RoundData,
    positionGuess: LatLng,
    score: Int,
    yearScore: Int,
    distanceScore: Int
) {
    if (showModal) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Results") },
            text = {
                Column {
                    Text("Total Score: $score")
                    Text("Year Score: $yearScore")
                    Text("Distance Score: $distanceScore")
                    Spacer(modifier = Modifier.height(16.dp))
                    ZoomableAsyncImage(
                        model = roundData.URL,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(roundData.Location.toLatLng(), 4f)
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        cameraPositionState = cameraPositionState
                    ) {
                        AdvancedMarker(state = MarkerState(position = roundData.Location.toLatLng()))
                        AdvancedMarker(state = MarkerState(position = positionGuess))
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

fun computeYearScore(yearInput: String, actualYear: String): Int {
    val yearsOff = kotlin.math.abs(yearInput.toInt() - actualYear.toInt())
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

fun computeDistanceScore(distanceInMeters: Float): Int {
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

@Preview(showBackground = true)
@Composable
fun RoundPlayPagePreview() {
    RoundPlayPage(
        rounds = listOf(
            RoundData(
                URL = "https://images.timeguessr.com/f58f4502-e13a-47e6-a7dd-f223951da34e.webp",
                Year = "2019",
                Location = Location(lat = 27.4722200350407, lng = 89.63841250287706),
                Description = "Two men carrying a painting of the King of Bhutan's family down a street in Thimphu, Bhutan.",
                License = "Keren Su/China Span / Alamy Stock Photo",
                Country = "Bhutan"
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun GuessPagePreview() {
    GuessPage(
        roundData = RoundData(
            URL = "https://images.timeguessr.com/f58f4502-e13a-47e6-a7dd-f223951da34e.webp",
            Year = "2019",
            Location = Location(lat = 27.4722200350407, lng = 89.63841250287706),
            Description = "Two men carrying a painting of the King of Bhutan's family down a street in Thimphu, Bhutan.",
            License = "Keren Su/China Span / Alamy Stock Photo",
            Country = "Bhutan"
        ),
        positionGuess = LatLng(27.4722200350407, 89.63841250287706),
        onSubmitGuess = {},
        onYearChange = {},
        year = ""
    )
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