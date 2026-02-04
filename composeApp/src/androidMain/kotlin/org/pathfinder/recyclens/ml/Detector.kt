package org.pathfinder.recyclens.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

data class DetectionResult(
    val boundingBox: RectF,
    val classId: Int,
    val confidence: Float,
    val label: String
)

fun decodeDetections(
    outputArray: FloatArray,
    numDetections: Int, // e.g. 8400
    numAttributes: Int, // e.g. 9 (4 box + 1 conf + 4 class probabilities, adjust accordingly)
    confidenceThreshold: Float = 0.5f
): List<DetectionResult> {
    val results = mutableListOf<DetectionResult>()

    // Loop through each detection candidate
    for (i in 0 until numDetections) {
        val offset = i * numAttributes
        // Extract bounding box (assuming normalized values)
        val cx = outputArray[offset]
        val cy = outputArray[offset + 1]
        val w = outputArray[offset + 2]
        val h = outputArray[offset + 3]
        // Object confidence
        val conf = outputArray[offset + 4]

        // Assume the remaining values are class probabilities.
        val numClasses = numAttributes - 5
        val classProbs = outputArray.sliceArray((offset + 5) until (offset + 5 + numClasses))
        val (maxProb, classId) = classProbs.withIndex().maxByOrNull { it.value }?.let { it.value to it.index } ?: continue

        val confidenceInClass = conf * maxProb
        if (confidenceInClass < confidenceThreshold) continue

        // Convert from center coordinates to top-left coordinates
        val left = cx - w / 2
        val top = cy - h / 2
        val right = cx + w / 2
        val bottom = cy + h / 2

        results.add(DetectionResult(RectF(left, top, right, bottom), classId, confidenceInClass, ""))
    }

    // Apply NMS here if you want to remove overlapping boxes.
    return applyNMS(results, iouThreshold = 0.5f)
}

fun applyNMS(
    detections: List<DetectionResult>,
    iouThreshold: Float
): List<DetectionResult> {
    val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
    val nmsResults = mutableListOf<DetectionResult>()
    while (sorted.isNotEmpty()) {
        val best = sorted.removeAt(0)
        nmsResults.add(best)
        sorted.removeAll { detection ->
            iou(best.boundingBox, detection.boundingBox) > iouThreshold
        }
    }
    return nmsResults
}

fun iou(a: RectF, b: RectF): Float {
    val intersection = RectF(
        maxOf(a.left, b.left),
        maxOf(a.top, b.top),
        minOf(a.right, b.right),
        minOf(a.bottom, b.bottom)
    )
    if (intersection.right < intersection.left || intersection.bottom < intersection.top) return 0f
    val intersectionArea = (intersection.right - intersection.left) * (intersection.bottom - intersection.top)
    val areaA = (a.right - a.left) * (a.bottom - a.top)
    val areaB = (b.right - b.left) * (b.bottom - b.top)
    return intersectionArea / (areaA + areaB - intersectionArea)
}

fun drawDetections(bitmap: Bitmap, detections: List<DetectionResult>): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.RGB_565, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    for (result in detections) {
        // Scale normalized coordinates to actual bitmap dimensions.
        val left = result.boundingBox.left * bitmap.width
        val top = result.boundingBox.top * bitmap.height
        val right = result.boundingBox.right * bitmap.width
        val bottom = result.boundingBox.bottom * bitmap.height
        canvas.drawRect(left, top, right, bottom, paint)
    }
    return mutableBitmap
}

