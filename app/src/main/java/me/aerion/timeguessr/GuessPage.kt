package me.aerion.timeguessr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
    positionGuess: LatLng?,
    year: Int,
    onYearChange: (Int) -> Unit,
    onSubmitGuess: (Guess) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val minYear = 1900
    val maxYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val defaultYear = (maxYear - minYear) / 2 + minYear

    val isSubmitEnabled = year in minYear..maxYear && positionGuess != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier.fillMaxSize().padding(bottom = 40.dp)
    ) {
        Text("Input your guess for the year")
        Spacer(Modifier.height(20.dp))
        TextField(
            value = when (year) {
                0 -> defaultYear.toString()
                -1 -> ""
                else -> year.toString()
            },
            onValueChange = { newValue ->
                if (newValue.isNotEmpty() && newValue.all { it.isDigit() }) {
                    onYearChange(newValue.toInt())
                } else {
                    onYearChange(-1)
                }
            },
            label = { Text("Year guess") },
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
        Spacer(Modifier.height(20.dp))
        Slider(
            value = if (year == 0) defaultYear.toFloat() else year.toFloat(),
            onValueChange = { onYearChange(it.toInt()) },
            valueRange = minYear.toFloat()..maxYear.toFloat(),
            steps = maxYear - minYear,
            modifier = Modifier.padding(horizontal = 40.dp),
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = {
                onSubmitGuess(Guess(year, positionGuess!!))
            },
            enabled = isSubmitEnabled,
            modifier = Modifier.height(48.dp)
        ) {
            Text("Submit Guess")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null)
        }
    }
}


@Preview(showBackground = true)
@Preview(
    "Landscape", showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun GuessPagePreview() {
    GuessPage(
        positionGuess = LatLng(27.4722200350407, 89.63841250287706),
        year = 0,
        onYearChange = {},
        onSubmitGuess = {}
    )
}