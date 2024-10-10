package me.aerion.timeguessr

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
    year: String,
    onYearChange: (String) -> Unit,
    onSubmitGuess: (Guess) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSubmitEnabled = year.isNotEmpty() && positionGuess != null

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
                onSubmitGuess(Guess(year.toInt(), positionGuess!!))
            },
            enabled = isSubmitEnabled
        ) {
            Text("Submit Guess")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.Done, contentDescription = null)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GuessPagePreview() {
    GuessPage(
        positionGuess = LatLng(27.4722200350407, 89.63841250287706),
        year = "",
        onYearChange = {},
        onSubmitGuess = {}
    )
}