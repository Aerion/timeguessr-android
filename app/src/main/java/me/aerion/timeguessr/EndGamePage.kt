package me.aerion.timeguessr

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import java.text.NumberFormat

@Composable
fun EndGamePage(
    roundResults: List<RoundResult>,
    dailyNumber: Int,
    modifier: Modifier = Modifier
) {
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
}

@Preview(showBackground = true)
@Preview(
    "Landscape", showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun EndGamePagePreview() {
    EndGamePage(
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