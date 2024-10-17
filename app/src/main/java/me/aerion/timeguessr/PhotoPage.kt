package me.aerion.timeguessr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@Composable
fun PhotoPage(roundData: RoundData, modifier: Modifier = Modifier) {
    val state = rememberZoomableImageState(rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = 10f)
    ))

    ZoomableAsyncImage(
        model = roundData.URL,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        state = state
    )
}