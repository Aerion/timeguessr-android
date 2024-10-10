package me.aerion.timeguessr

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@SuppressLint("UnrememberedMutableState")
@Composable
fun RoundResultPage(
    onDismiss: () -> Unit,
    roundData: RoundData,
    positionGuess: LatLng,
    score: Int,
    yearScore: Int,
    distanceScore: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Results") },
        text = {
            Column {
                Text("Total Score: $score")
                Text("Year Score: $yearScore")
                Text("Distance Score: $distanceScore")
                Spacer(modifier = Modifier.height(16.dp))
                ZoomableAsyncImage(
                    model = roundData.URL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(roundData.Location.toLatLng(), 4f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    AdvancedMarker(state = MarkerState(position = roundData.Location.toLatLng()))
                    AdvancedMarker(state = MarkerState(position = positionGuess))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}