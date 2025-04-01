package me.aerion.timeguessr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aerion.timeguessr.ui.theme.TimeguessrTheme

// TODO: Add result map with all of the guesses
// TODO: Reduce the size of the app
// TODO: Publish on app store

enum class Page {
    RoundPlayPage,
    RoundResultPage,
    EndGamePage
}

class MainActivity : ComponentActivity() {
    private val roundDataSource: RoundDataFetcher = NetworkRoundDataFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var loadingStatus by rememberSaveable { mutableStateOf("LOADING") }
            var retryRoundFetchTrigger by rememberSaveable { mutableIntStateOf(0) }
            var rounds by rememberSaveable { mutableStateOf<List<RoundData>?>(null) }
            var currentPage by rememberSaveable { mutableStateOf(Page.RoundPlayPage) }
            var currentRoundIndex by rememberSaveable { mutableIntStateOf(0) }
            var roundResults by rememberSaveable { mutableStateOf<List<RoundResult>>(emptyList()) }
            val endGameRefreshCorountineScope = CoroutineScope(context = Dispatchers.IO)
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(retryRoundFetchTrigger) {
                loadingStatus = "LOADING"
                currentRoundIndex = 0
                rounds = null
                roundResults = emptyList()
                currentPage = Page.RoundPlayPage

                if (retryRoundFetchTrigger > 0) {
                    delay(1000)
                }

                val roundsList = roundDataSource.fetchRounds()
                Log.i("TimeGuessr", "Rounds: $roundsList")

                if (roundsList == null) {
                    loadingStatus = "ERROR"
                    return@LaunchedEffect
                }

                rounds = roundsList
                loadingStatus = "LOADED"
            }

            LifecycleResumeEffect(Unit) {
                if (currentPage == Page.EndGamePage) {
                    endGameRefreshCorountineScope.launch {
                        try {
                            Log.d("TimeGuessr", "Checking for new daily")
                            val newRoundsList = roundDataSource.fetchRounds()
                            if (newRoundsList != null && newRoundsList[0].No > rounds!![0].No) {
                                Log.d("TimeGuessr", "New daily available")
                                if (snackbarHostState.showSnackbar(
                                        message = "New daily available",
                                        actionLabel = "Update"
                                    ) == SnackbarResult.ActionPerformed
                                ) {
                                    // Trigger a fresh round fetch.
                                    // One could also just update the rounds list here, but this way we're sure to always get the latest data.
                                    retryRoundFetchTrigger++
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TimeGuessr", "Error in coroutine fetching end game new daily", e)
                        }
                    }
                }

                onPauseOrDispose {
                }
            }

            TimeguessrTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding().navigationBarsPadding(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { innerPadding ->
                    // Remove the warning about unused innerPadding
                    val unused = innerPadding;

                    if (loadingStatus == "ERROR") {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text("An error occurred while fetching the rounds.")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { retryRoundFetchTrigger++ }) {
                                Text("Retry")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.RestartAlt, modifier = Modifier.width(18.dp), contentDescription = null)
                            }
                        }
                        return@Scaffold
                    }

                    // Show a loader while the data is being fetched
                    if (loadingStatus == "LOADING") {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text("Loading the rounds...")
                            Spacer(Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }
                        return@Scaffold
                    }

                    val currentRound = rounds!![currentRoundIndex]
                    val totalScore = roundResults.sumOf { it.totalScore }

                    when (currentPage) {
                        Page.RoundPlayPage -> RoundPlayPage(
                            round = currentRound,
                            currentRoundIndex = currentRoundIndex,
                            totalScore = totalScore,
                            onRoundSubmit = { guess ->
                                val distanceInMeters = computeDistanceInMeters(
                                    guess.position.latitude,
                                    guess.position.longitude,
                                    currentRound.Location.lat,
                                    currentRound.Location.lng
                                )
                                roundResults = roundResults + RoundResult(
                                    yearScore = computeYearScore(
                                        guess.year,
                                        currentRound.Year.toInt()
                                    ),
                                    distanceScore = computeDistanceScore(distanceInMeters),
                                    guess = guess,
                                    distanceDiffInMeters = distanceInMeters
                                )
                                currentPage = Page.RoundResultPage
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        Page.RoundResultPage -> RoundResultPage(
                            round = currentRound,
                            roundResult = roundResults.last(),
                            modifier = Modifier.fillMaxSize(),
                            isLastRound = currentRoundIndex == rounds!!.size - 1,
                            onNextRound = {
                                if (currentRoundIndex < rounds!!.size - 1) {
                                    currentRoundIndex++
                                    currentPage = Page.RoundPlayPage
                                } else {
                                    currentPage = Page.EndGamePage
                                }
                            }
                        )

                        Page.EndGamePage -> EndGamePage(
                            roundResults = roundResults,
                            dailyNumber = currentRound.No.toInt(),
                            modifier = Modifier.fillMaxSize()
                        )
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
}