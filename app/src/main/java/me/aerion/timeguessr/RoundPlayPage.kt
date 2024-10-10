package me.aerion.timeguessr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import java.text.NumberFormat

@Composable
fun RoundPlayPage(round: RoundData, onRoundSubmit: (Guess) -> Unit,
                  totalScore: Int, currentRoundIndex: Int,
                  modifier: Modifier = Modifier) {
    var positionGuess by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var year by rememberSaveable { mutableStateOf("") }

    val totalScoreString = NumberFormat.getNumberInstance().format(totalScore)

    Column(modifier.fillMaxSize()) {
        // Display current score and progress
        Text(
            text = "Score: $totalScoreString | Round: ${currentRoundIndex + 1}/5",
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> PhotoPage(round, modifier)
                1 -> MapPage(
                    onPositionGuessChange = { positionGuess = it },
                    positionGuess = positionGuess,
                    modifier = modifier
                )
                2 -> GuessPage(
                    positionGuess = positionGuess,
                    year = year,
                    onYearChange = { year = it },
                    onSubmitGuess = {
                        onRoundSubmit(Guess(year.toInt(), positionGuess!!))
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
}

@Preview(showBackground = true)
@Composable
fun RoundPlayPagePreview() {
    RoundPlayPage(
        round = RoundData(
            URL = "https://images.timeguessr.com/f58f4502-e13a-47e6-a7dd-f223951da34e.webp",
            Year = "2019",
            Location = Location(lat = 27.4722200350407, lng = 89.63841250287706),
            Description = "Two men carrying a painting of the King of Bhutan's family down a street in Thimphu, Bhutan.",
            License = "Keren Su/China Span / Alamy Stock Photo",
            Country = "Bhutan"
        ),
        onRoundSubmit = {},
        totalScore = 12345,
        currentRoundIndex = 2
    )
}
