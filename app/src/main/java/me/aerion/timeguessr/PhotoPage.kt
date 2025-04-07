package me.aerion.timeguessr

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.ZoomableImageState
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage

@Composable
fun PhotoPage(
    photoZoomState: ZoomableImageState,
    imageModel: ImageRequest?,
    loadingStatus: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (loadingStatus == "LOADING") {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Loading image...")
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator()
        }
        return
    }

    if (loadingStatus == "ERROR") {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("An error occurred while fetching the photo.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.RestartAlt,
                    modifier = Modifier.width(18.dp),
                    contentDescription = null
                )
            }
        }
        return
    }

    ZoomableAsyncImage(
        model = imageModel,
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        state = photoZoomState,
        onDoubleClick = IncrementalZoomOnDoubleClick()
    )
}