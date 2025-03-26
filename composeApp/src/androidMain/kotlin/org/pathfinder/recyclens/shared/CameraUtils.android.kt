package org.pathfinder.recyclens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.pathfinder.recyclens.interfaces.CameraController
import org.pathfinder.recyclens.AndroidCameraController

@Composable
actual fun rememberCameraController(): CameraController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember { AndroidCameraController(context, lifecycleOwner) }
}