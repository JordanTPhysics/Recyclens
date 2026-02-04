package org.pathfinder.recyclens.views

import androidx.compose.runtime.Composable
import org.pathfinder.recyclens.SharedImageViewModel
import org.pathfinder.recyclens.interfaces.CameraController

@Composable
actual fun CameraView(
    controller: CameraController,
    onNavigateToRecycle: () -> Unit,
    onNavigateToReport: () -> Unit,
    sharedImageViewModel: SharedImageViewModel
) {
}