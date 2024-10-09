package me.aerion.timeguessr

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.google.android.gms.maps.model.LatLng

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
        Button(
            onClick = {
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