package me.aerion.timeguessr

import android.graphics.ColorSpace
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.ZoomableImageState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Composable
fun PhotoPage(
    roundData: RoundData,
    photoZoomState: ZoomableImageState,
    modifier: Modifier = Modifier
) {
    ZoomableAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(roundData.URL)
            .colorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
            .build(),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        state = photoZoomState
    )
}