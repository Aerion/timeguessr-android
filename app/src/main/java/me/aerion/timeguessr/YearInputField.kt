package me.aerion.timeguessr

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun YearInputField() {
    var year by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = year,
        onValueChange = { newValue ->
            // Allow only numeric input and limit to 4 characters
            if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                year = newValue
            }
        },
        label = { Text("Enter Year") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                // Close the keyboard when Enter is pressed
                keyboardController?.hide()
            }
        )
    )
}