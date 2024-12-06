package me.aerion.timeguessr

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomableState

@Immutable
internal class IncrementalZoomOnDoubleClick(
    private val incrementFactor: Float = 2f,
    private val maxZoomFactor: Float? = null
) : DoubleClickToZoomListener {
    override suspend fun onDoubleClick(state: ZoomableState, centroid: Offset) {
        val transformation = state.contentTransformation.takeIf { it.isSpecified } ?: return // Content isn't ready yet
        val maxZoomFactor = this.maxZoomFactor ?: state.zoomSpec.maxZoomFactor
        val newZoomFactor = transformation.scale.scaleX * incrementFactor

        if (newZoomFactor >= maxZoomFactor) {
            state.resetZoom()
        } else {
            state.zoomTo(
                zoomFactor = newZoomFactor,
                centroid = centroid,
            )
        }
    }
}