package org.pathfinder.recyclens.ml

import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.scale
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

fun preprocessImage(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): TensorImage {
    val resizedBitmap = bitmap.scale(targetWidth, targetHeight, false)
    // Create a TensorImage and load the bitmap
    val tensorImage = TensorImage(DataType.FLOAT32)

    val numPixels = targetWidth * targetHeight
    val byteBuffer = ByteBuffer.allocateDirect(3 * numPixels * 4)
    byteBuffer.order(ByteOrder.nativeOrder())

    // Get pixel data from the resized bitmap.
    val pixels = IntArray(numPixels)
    resizedBitmap.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
    Log.d("PreprocessImage", "pixels: ${pixels.size}")
    Log.d("PreprocessImage", "byteBuffer size: ${byteBuffer.capacity()}")
    // For each pixel, extract R, G, and B and normalize them (e.g. dividing by 255).
    var i = 0
    for (pixel in pixels) {
        // Even though the bitmap is ARGB_8888, we only need RGB.
        val r = ((pixel shr 16) and 0xFF).toFloat() / 255.0f
        val g = ((pixel shr 8) and 0xFF).toFloat() / 255.0f
        val b = (pixel and 0xFF).toFloat() / 255.0f


//        val r = ((pixel shr 16) and 0xFF).toFloat()
//        val g = ((pixel shr 8) and 0xFF).toFloat()
//        val b = (pixel and 0xFF).toFloat()
//        // Calculate grayscale value using the luminance formula.
//        val gray = 0.299f * r + 0.587f * g + 0.114f * b
//        // Normalize to [0, 1]
//        val normalizedGray = gray / 255.0f
        try {
//            byteBuffer.putFloat(normalizedGray)
                    byteBuffer.putFloat(r)
                    byteBuffer.putFloat(g)
                    byteBuffer.putFloat(b)
            i += 1

        } catch (e: Exception) {
            Log.d("PreprocessImage", "Error: $e on iteration step: $i.")
            throw e
        }
    }
    byteBuffer.rewind()

    Log.d("PreprocessImage", "byteBuffer size: ${byteBuffer.capacity()}")

    val tensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1, targetHeight, targetWidth, 3), DataType.FLOAT32)
    tensorBuffer.loadBuffer(byteBuffer)

    Log.d("PreprocessImage", "byteBuffer size: ${tensorBuffer.shape}")


    tensorImage.load(tensorBuffer)

    Log.d("PreprocessImage", "byteBuffer size: ${tensorImage.buffer.capacity()}")

//    return tensorImage

    val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(0.0f, 255.0f))
        .build()
    return imageProcessor.process(tensorImage)
}


