package org.pathfinder.recyclens.navigation

sealed class Screen {
    data class Camera(val cameraMode: Int) : Screen()
    data object Recycle : Screen()
    data object Report : Screen()
}