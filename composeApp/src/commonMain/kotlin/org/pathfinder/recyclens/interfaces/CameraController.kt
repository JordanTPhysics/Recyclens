package org.pathfinder.recyclens.interfaces

import org.pathfinder.recyclens.shared.CameraMode

interface CameraController {
    var cameraMode: CameraMode
    fun takePicture(onPhotoTaken: (ByteArray) -> Unit)
    fun setMode(mode: Int)
}