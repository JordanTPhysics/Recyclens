package org.pathfinder.recyclens.interfaces

interface CameraController {
    fun takePicture(onPhotoTaken: (ByteArray) -> Unit)
    fun setMode(mode: Int)
}