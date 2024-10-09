package me.aerion.timeguessr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.aerion.timeguessr.ui.theme.TimeguessrTheme


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

