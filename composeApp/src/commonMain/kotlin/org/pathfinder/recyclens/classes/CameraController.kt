package org.pathfinder.recyclens.classes

expect class CameraController {
    fun takePicture(onPhotoTaken: (ByteArray) -> Unit)
    fun startCamera()
    fun setMode(mode: Int)
}