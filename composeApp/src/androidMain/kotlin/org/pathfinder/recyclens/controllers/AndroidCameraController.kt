package org.pathfinder.recyclens

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.pathfinder.recyclens.interfaces.CameraController
import org.pathfinder.recyclens.shared.CameraMode
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AndroidCameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : CameraController {

    private var imageCapture: ImageCapture? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraMode = CameraMode.Recycle

     fun startCamera(previewView: androidx.camera.view.PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun takePicture(onPhotoTaken: (ByteArray) -> Unit) {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                image.close()
                onPhotoTaken(bytes) // Return the ByteArray directly
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
            }
        })
    }

    override fun setMode(mode: Int) {
        cameraMode = when (mode) {
            1 -> CameraMode.Recycle
            2 -> CameraMode.Report
            else -> CameraMode.Recycle
        }
    }
}

