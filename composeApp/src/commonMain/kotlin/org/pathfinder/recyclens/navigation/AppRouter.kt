package org.pathfinder.recyclens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.pathfinder.recyclens.SharedImageViewModel
import org.pathfinder.recyclens.shared.CameraMode
import org.pathfinder.recyclens.shared.rememberCameraController
import org.pathfinder.recyclens.views.CameraView
import org.pathfinder.recyclens.views.RecycleView
import org.pathfinder.recyclens.views.ReportView

@Composable
fun AppRouter(sharedImageViewModel: SharedImageViewModel) {
    var currentView by remember { mutableStateOf<Screen>(Screen.Camera(CameraMode.Recycle.ordinal)) }
    val cameraController = rememberCameraController()

    when (currentView) {
        is Screen.Camera -> {

            CameraView(
                controller = cameraController,
                onNavigateToRecycle = { currentView = Screen.Recycle },
                onNavigateToReport = { currentView = Screen.Report },
                sharedImageViewModel = sharedImageViewModel
            )
        }
        is Screen.Recycle -> {
            RecycleView(
                onBack = { currentView = Screen.Camera(CameraMode.Recycle.ordinal) },
                sharedImageViewModel = sharedImageViewModel
            )
        }
        is Screen.Report -> {
            ReportView(
                onBack = { currentView = Screen.Camera(CameraMode.Report.ordinal) },
                sharedImageViewModel = sharedImageViewModel
            )
        }
    }
}
