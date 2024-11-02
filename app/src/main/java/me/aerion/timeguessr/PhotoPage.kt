package me.aerion.timeguessr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.saket.telephoto.zoomable.ZoomableImageState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Composable
fun PhotoPage(
    roundData: RoundData,
    photoZoomState: ZoomableImageState,
    modifier: Modifier = Modifier
) {

    ZoomableAsyncImage(
        model = roundData.URL,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        state = photoZoomState
    )
}