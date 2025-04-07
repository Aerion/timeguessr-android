package me.aerion.timeguessr

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import java.text.NumberFormat

@Composable
fun RoundPlayPage(
    round: RoundData,
    onRoundSubmit: (Guess) -> Unit,
    totalScore: Int,
    currentRoundIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var positionGuess by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var year by rememberSaveable { mutableIntStateOf(0) }
    var imageLoadingRetryCount by remember { mutableIntStateOf(0) }
    var imageRequest by remember { mutableStateOf<ImageRequest?>(null) }
    var imageLoadingStatus by remember { mutableStateOf("LOADING") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            positionGuess ?: LatLng(
                51.48633971492552,
                3.691691980292835
            ), 4f
        )
    }

    val photoZoomState = rememberZoomableImageState(
        rememberZoomableState(
            zoomSpec = ZoomSpec(maxZoomFactor = 10f)
        )
    )

    val totalScoreString = NumberFormat.getNumberInstance().format(totalScore)

    LaunchedEffect(round.URL, imageLoadingRetryCount) {
        Log.d(
            "RoundPlayPage",
            "status=${imageLoadingStatus} Loading photo from URL: ${round.URL}, retryCount: $imageLoadingRetryCount"
        )

        imageLoadingStatus = "LOADING"

        if (imageLoadingRetryCount > 0) {
            delay(1000)
        }

        val url =
            round.URL + if (imageLoadingRetryCount > 0) "?retry=$imageLoadingRetryCount" else ""
        imageRequest = ImageRequest.Builder(context)
            .data(url)
            .build()
        val result = ImageLoader(context).execute(imageRequest!!)

        if (result is SuccessResult) {
            imageLoadingStatus = "SUCCESS"
        } else {
            imageLoadingStatus = "ERROR"
        }
    }

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth > maxHeight) {
            Row(modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Round: ${currentRoundIndex + 1}/5",
                        )
                        Text(
                            text = "Score: $totalScoreString"
                        )
                    }
                    when (selectedTabIndex) {
                        0 -> PhotoPage(
                            photoZoomState,
                            imageRequest,
                            imageLoadingStatus,
                            onRetry = { imageLoadingRetryCount++ },
                            modifier
                        )

                        1 -> MapPage(
                            onPositionGuessChange = { positionGuess = it },
                            positionGuess = positionGuess,
                            cameraPositionState = cameraPositionState,
                            modifier = modifier
                        )

                        2 -> GuessPage(
                            positionGuess = positionGuess,
                            year = year,
                            onYearChange = { year = it },
                            onSubmitGuess = {
                                onRoundSubmit(Guess(year, positionGuess!!))
                            },
                            modifier = modifier
                        )
                    }
                }
                NavigationRail {
                    Spacer(Modifier.weight(1f))
                    NavigationRailItem(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null
                            )
                        },
                        label = { Text("Photo") }
                    )
                    NavigationRailItem(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        icon = { Icon(imageVector = Icons.Default.Map, contentDescription = null) },
                        label = { Text("Map") }
                    )
                    NavigationRailItem(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null
                            )
                        },
                        label = { Text("Guess") }
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        } else {
            Column(modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Round: ${currentRoundIndex + 1}/5",
                    )
                    Text(
                        text = "Score: $totalScoreString"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> PhotoPage(
                            photoZoomState,
                            imageRequest,
                            imageLoadingStatus,
                            onRetry = { imageLoadingRetryCount++ },
                            modifier
                        )

                        1 -> MapPage(
                            onPositionGuessChange = { positionGuess = it },
                            positionGuess = positionGuess,
                            cameraPositionState = cameraPositionState,
                            modifier = modifier
                        )

                        2 -> GuessPage(
                            positionGuess = positionGuess,
                            year = year,
                            onYearChange = { year = it },
                            onSubmitGuess = {
                                onRoundSubmit(Guess(year, positionGuess!!))
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
                        Icon(imageVector = Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Photo")
                    }
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(imageVector = Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Map")
                    }
                    Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Guess")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(
    "Landscape", showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun RoundPlayPagePreview() {
    RoundPlayPage(
        round = RoundData(
            URL = "https://images.timeguessr.com/f58f4502-e13a-47e6-a7dd-f223951da34e.webp",
            Year = "2019",
            Location = Location(lat = 27.4722200350407, lng = 89.63841250287706),
            Description = "Two men carrying a painting of the King of Bhutan's family down a street in Thimphu, Bhutan.",
            License = "Keren Su/China Span / Alamy Stock Photo",
            Country = "Bhutan",
            No = "123"
        ),
        onRoundSubmit = {},
        totalScore = 12345,
        currentRoundIndex = 2
    )
}
