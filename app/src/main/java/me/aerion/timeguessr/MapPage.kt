package me.aerion.timeguessr

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

@Composable
fun MapPage(
    onPositionGuessChange: (LatLng) -> Unit,
    markerStateGuess: MarkerState?,
    onMarkerStateGuessChange: (MarkerState?) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialLocation = LatLng(51.48633971492552, 3.691691980292835)
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
            onMarkerStateGuessChange(MarkerState(position = position))
        }
    ) {
        // Show the guess from the user
        if (markerStateGuess != null) {
            AdvancedMarker(
                state = markerStateGuess,
            )
        }
    }
}