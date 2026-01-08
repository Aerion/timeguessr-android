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
import androidx.compose.runtime.rememberCoroutineScope
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
    private lateinit var appStateRepository: AppStateRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the repository
        appStateRepository = AppStateRepository(this)

        setContent {
            var loadingStatus by rememberSaveable { mutableStateOf("LOADING") }
            var retryRoundFetchTrigger by rememberSaveable { mutableIntStateOf(0) }
            var rounds by rememberSaveable { mutableStateOf<List<RoundData>?>(null) }
            var currentPage by rememberSaveable { mutableStateOf(Page.RoundPlayPage) }
            var currentRoundIndex by rememberSaveable { mutableIntStateOf(0) }
            var roundResults by rememberSaveable { mutableStateOf<List<RoundResult>>(emptyList()) }
            val endGameRefreshCorountineScope = CoroutineScope(Dispatchers.IO)
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            suspend fun fetchNewRounds() {
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
                    return
                }

                rounds = roundsList
                loadingStatus = "LOADED"
            }

            suspend fun showNewDailySnackbar() {
                if (snackbarHostState.currentSnackbarData != null) {
                    // Snackbar is already shown
                    return
                }

                if (snackbarHostState.showSnackbar(
                        message = "New daily available",
                        actionLabel = "Update",
                        withDismissAction = true,
                    ) == SnackbarResult.ActionPerformed
                ) {
                    // Trigger a fresh round fetch.
                    // One could also just update the rounds list here, but this way we're sure to always get the latest data.
                    fetchNewRounds()
                }
            }

            // Load saved state when the app starts
            LaunchedEffect(Unit) {
                try {
                    val savedRounds = appStateRepository.getSavedRounds()
                    val savedRoundResults = appStateRepository.getSavedRoundResults()
                    val savedCurrentRoundIndex = appStateRepository.getSavedCurrentRoundIndex()
                    val savedCurrentPage = appStateRepository.getSavedCurrentPage()

                    if (savedRounds != null) {
                        Log.d("TimeGuessr", "Using saved state")

                        // Check if there are new rounds available
                        val newRounds = roundDataSource.fetchRounds()
                        if (newRounds != null && newRounds[0].No > savedRounds[0].No) {
                            Log.d("TimeGuessr", "New rounds available")
                            endGameRefreshCorountineScope.launch {
                                showNewDailySnackbar()
                            }
                        }

                        rounds = savedRounds
                        roundResults = savedRoundResults
                        currentRoundIndex = savedCurrentRoundIndex
                        currentPage = savedCurrentPage
                        loadingStatus = "LOADED"
                    } else {
                        // No saved state, proceed with initial loading
                        Log.d("TimeGuessr", "No saved state found, fetching new rounds")
                        fetchNewRounds()
                    }
                } catch (e: Exception) {
                    Log.e("TimeGuessr", "Error loading saved state", e)
                    fetchNewRounds()
                }
            }

            // Save state whenever it changes
            LaunchedEffect(rounds, roundResults, currentRoundIndex, currentPage) {
                if (rounds != null) {
                    Log.d("TimeGuessr", "Saving app state")
                    coroutineScope.launch {
                        appStateRepository.saveAppState(
                            rounds = rounds,
                            roundResults = roundResults,
                            currentRoundIndex = currentRoundIndex,
                            currentPage = currentPage
                        )
                    }
                }
            }

            LaunchedEffect(retryRoundFetchTrigger) {
                if (retryRoundFetchTrigger > 0) {
                    fetchNewRounds()
                }
            }

            LifecycleResumeEffect(Unit) {
                if (loadingStatus == "LOADED") {
                    endGameRefreshCorountineScope.launch {
                        try {
                            Log.d("TimeGuessr", "Checking for new daily")
                            val newRoundsList = roundDataSource.fetchRounds()
                            if (newRoundsList != null && rounds != null && newRoundsList[0].No > rounds!![0].No) {
                                Log.d("TimeGuessr", "New daily available")
                                showNewDailySnackbar()
                            }
                        } catch (e: Exception) {
                            Log.e("TimeGuessr", "Error in coroutine fetching end game new daily", e)
                        }
                    }
                }

                onPauseOrDispose {
                    // Save state when app goes to background
                    coroutineScope.launch {
                        if (rounds != null) {
                            appStateRepository.saveAppState(
                                rounds = rounds,
                                roundResults = roundResults,
                                currentRoundIndex = currentRoundIndex,
                                currentPage = currentPage
                            )
                        }
                    }
                }
            }

            TimeguessrTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
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
                                Icon(
                                    Icons.Default.RestartAlt,
                                    modifier = Modifier.width(18.dp),
                                    contentDescription = null
                                )
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
                            rounds = rounds!!,
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