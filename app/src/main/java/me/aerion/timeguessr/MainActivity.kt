package me.aerion.timeguessr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.aerion.timeguessr.ui.theme.TimeguessrTheme


// TODO: Restrict api key
// TODO: Colors
// TODO: Emoji result

enum class Page {
    RoundPlayPage,
    RoundResultPage,
    EndGamePage
}

class MainActivity : ComponentActivity() {
    private val roundDataSource: RoundDataFetcher = StubbedRoundDataFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeguessrTheme {
                val rounds = remember { mutableStateOf<List<RoundData>?>(null) }
                var currentPage by remember { mutableStateOf(Page.RoundPlayPage) }
                var currentRoundIndex by remember { mutableIntStateOf(0) }
                var roundResults by remember { mutableStateOf<List<RoundResult>>(emptyList()) }

                LaunchedEffect(Unit) {
                    rounds.value = roundDataSource.fetchRounds()
                }

                // Show a loader while the data is being fetched
                if (rounds.value == null) {
                    Text("Loading...")
                    return@TimeguessrTheme
                }

                val currentRound = rounds.value!![currentRoundIndex]
                val totalScore = roundResults.sumOf { it.totalScore }

                when (currentPage) {
                    Page.RoundPlayPage -> RoundPlayPage(
                        round = currentRound,
                        currentRoundIndex = currentRoundIndex,
                        totalScore = totalScore,
                        onRoundSubmit = { guess ->
                            roundResults = roundResults + RoundResult(
                                yearScore = computeYearScore(guess.year, currentRound.Year.toInt()),
                                distanceScore = computeDistanceScore(
                                    guess.position.latitude,
                                    guess.position.longitude,
                                    currentRound.Location.lat,
                                    currentRound.Location.lng
                                ),
                                guess = guess,
                            )
                            currentPage = Page.RoundResultPage
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Page.RoundResultPage -> RoundResultPage(
                        round = currentRound,
                        roundResult = roundResults.last(),
                        modifier = Modifier.fillMaxSize(),
                        onNextRound = {
                            if (currentRoundIndex < rounds.value!!.size - 1) {
                                currentRoundIndex++
                                currentPage = Page.RoundPlayPage
                            } else {
                                currentPage = Page.EndGamePage
                            }
                        }
                    )
                    /*Page.EndGamePage -> EndGamePage(
                        modifier = Modifier.fillMaxSize(),
                        onRestart = {
                            currentRoundIndex = 0
                            currentPage = Page.RoundPlayPage
                        }
                    )*/
                    else -> {}
                }

                BackHandlerWithConfirmation(
                    title = "Exit TimeGuessr?",
                    text = "Are you sure you want to exit TimeGuessr?",
                    onConfirm = { finish() },
                    onDismiss = { /* Do nothing */ }
                )
            }
        }
    }
}