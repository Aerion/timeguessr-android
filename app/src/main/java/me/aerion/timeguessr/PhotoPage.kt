package me.aerion.timeguessr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.request.ImageRequest
import me.saket.telephoto.zoomable.ZoomableImageState
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPage(
    roundData: RoundData,
    photoZoomState: ZoomableImageState,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }
    val url = roundData.URL + if (retryCount > 0) "?retry=$retryCount" else ""

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            retryCount++
            isRefreshing = false
        },
    ) {
        ZoomableAsyncImage(
            model = url,
            contentDescription = null,
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            state = photoZoomState,
            onDoubleClick = IncrementalZoomOnDoubleClick()
        )
    }
}