package me.aerion.timeguessr

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import java.text.NumberFormat

@Composable
fun EndGamePage(
    rounds: List<RoundData>,
    roundResults: List<RoundResult>,
    dailyNumber: Int,
    modifier: Modifier = Modifier
) {
    var isInReviewMode by rememberSaveable { mutableStateOf(false) }

    if (isInReviewMode) {
        BackHandler { isInReviewMode = false }
    }

    if (!isInReviewMode) {
        // Summary view
        val context = LocalContext.current
        val totalScoreString =
            NumberFormat.getNumberInstance().format(roundResults.sumOf { it.totalScore })
        val maxScoreString = NumberFormat.getNumberInstance().format(roundResults.size * 10000)

        val scoreBuilder = StringBuilder()
        scoreBuilder.appendLine("Timeguessr #$dailyNumber $totalScoreString/$maxScoreString")
        for (roundResult in roundResults) {
            scoreBuilder.append(getGeoScoreString(roundResult.distanceScore))
            scoreBuilder.append(getTimeScoreString(roundResult.yearScore))
            scoreBuilder.appendLine()
        }

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Total Score: ${totalScoreString}/${maxScoreString}")

            Spacer(modifier = Modifier.height(6.dp))

            for (roundResult in roundResults) {
                Text(
                    "${getGeoScoreString(roundResult.distanceScore)}${
                        getTimeScoreString(roundResult.yearScore)
                    }"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Timeguessr #$dailyNumber")

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = { isInReviewMode = true }
            ) {
                Text("Review Rounds")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, scoreBuilder.toString())
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share results via"))
                }
            ) {
                Text("Share results")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Share, modifier = Modifier.width(18.dp), contentDescription = null)
            }
        }
    } else {
        // Review mode view
        ReviewModeView(
            rounds = rounds,
            roundResults = roundResults,
            onExitReview = { isInReviewMode = false },
            modifier = modifier
        )
    }
}

@Composable
private fun ReviewModeView(
    rounds: List<RoundData>,
    roundResults: List<RoundResult>,
    onExitReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0) { rounds.size }

    Column(modifier = modifier) {
        // Top bar with back button and page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExitReview) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to summary")
            }
            Text(
                "Round ${pagerState.currentPage + 1}/${rounds.size}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance layout
        }

        // HorizontalPager for swiping between rounds
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            RoundReviewPage(
                round = rounds[page],
                roundResult = roundResults[page],
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun RoundReviewPage(
    round: RoundData,
    roundResult: RoundResult,
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
            }
        } else {
            // Landscape layout
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
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
                RoundScoreCard(
                    totalScoreString = totalScoreString,
                    maxScoreString = maxScoreString,
                    modifier = Modifier.fillMaxWidth()
                )
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
fun EndGamePagePreview() {
    EndGamePage(
        rounds = List(5) { index ->
            RoundData(
                No = "123",
                URL = "https://example.com/image$index.jpg",
                Year = "${2000 + index * 5}",
                Location = Location(51.5 + index, 3.7 + index),
                Description = "Historical photo ${index + 1}",
                License = "CC BY-SA",
                Country = "Country"
            )
        },
        roundResults = listOf(
            RoundResult(
                yearScore = 5000,
                distanceScore = 1000,
                distanceDiffInMeters = 1242,
                guess = Guess(
                    year = 2000,
                    position = LatLng(0.0, 0.0)
                )
            ),
            RoundResult(
                yearScore = 4000,
                distanceScore = 2100,
                distanceDiffInMeters = 11,
                guess = Guess(
                    year = 2000,
                    position = LatLng(0.0, 0.0)
                )
            ),
            RoundResult(
                yearScore = 3000,
                distanceScore = 3200,
                distanceDiffInMeters = 12381293,
                guess = Guess(
                    year = 2000,
                    position = LatLng(0.0, 0.0)
                )
            ),
            RoundResult(
                yearScore = 2000,
                distanceScore = 4300,
                distanceDiffInMeters = 12321,
                guess = Guess(
                    year = 2000,
                    position = LatLng(0.0, 0.0)
                )
            ),
            RoundResult(
                yearScore = 5000,
                distanceScore = 12,
                distanceDiffInMeters = 0,
                guess = Guess(
                    year = 2000,
                    position = LatLng(0.0, 0.0)
                )
            )
        ),
        modifier = Modifier.fillMaxSize(),
        dailyNumber = 123
    )
}