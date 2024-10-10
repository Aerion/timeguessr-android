package me.aerion.timeguessr

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapPage(
    onPositionGuessChange: (LatLng) -> Unit,
    positionGuess: LatLng?,
    modifier: Modifier = Modifier
) {
    val initialLocation = positionGuess ?: LatLng(51.48633971492552, 3.691691980292835)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 4f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            indoorLevelPickerEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        onMapClick = { position ->
            onPositionGuessChange(position)
        }
    ) {
        // Show the guess from the user
        if (positionGuess != null) {
            AdvancedMarker(
                state = MarkerState(position = positionGuess),
            )
        }
    }
}