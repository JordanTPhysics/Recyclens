package org.pathfinder.recyclens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.pathfinder.recyclens.interfaces.CameraController
import org.pathfinder.recyclens.ml.DetectionResult
import org.pathfinder.recyclens.ml.YoloDetector
import org.pathfinder.recyclens.ml.decodeDetections
import org.pathfinder.recyclens.ml.preprocessImage
import org.pathfinder.recyclens.shared.CameraMode
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AndroidCameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : CameraController {

    override var cameraMode: CameraMode = CameraMode.Recycle
    private var imageCapture: ImageCapture? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    val detector = YoloDetector(context)

    fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // NV21 format: Y + VU interleaved
        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y channel
        yBuffer.get(nv21, 0, ySize)

        // The U and V planes are not interleaved; we need to interleave them manually.
        // NV21 expects V first, then U.
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        // Convert NV21 byte array to Bitmap
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    @OptIn(ExperimentalGetImage::class)
    fun startCamera(previewView: PreviewView, onPredictions: (List<DetectionResult>) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(executor, { imageProxy ->
                val bitmap = imageProxyToBitmap(imageProxy)
                val tensorImage = preprocessImage(bitmap!!, 320, 320)
                val results = detector.detect(tensorImage)
                Log.d("CameraX", "Results: $results")
                val detectedObjects = decodeDetections(results, 8400, 9)
                onPredictions(detectedObjects)
                imageProxy.close()
            })

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
            0 -> CameraMode.Recycle
            1 -> CameraMode.Report
            else -> CameraMode.Recycle
        }
    }
}

