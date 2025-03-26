package org.pathfinder.recyclens.classes

actual class CameraController {
    actual fun takePicture(onPhotoTaken: (ByteArray) -> Unit) {
    }

    actual fun startCamera() {
    }

    actual fun setMode(mode: Int) {
    }
}