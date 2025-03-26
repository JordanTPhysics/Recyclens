package org.pathfinder.recyclens

import androidx.compose.runtime.Composable
import org.pathfinder.recyclens.interfaces.CameraController

@Composable
expect fun CameraView(controller: CameraController)