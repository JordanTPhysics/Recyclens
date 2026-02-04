package org.pathfinder.recyclens.ml

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.channels.FileChannel

class YoloDetector(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelName = "yolov5s_f16.tflite" // ensure this is in assets

    // Setup the interpreter
    fun setup() {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(modelBuffer, Interpreter.Options().apply {
            numThreads = 4
        })
    }

    fun clear() {
        interpreter?.close()
        interpreter = null
    }

    /**
     * Run inference given a preprocessed image.
     * Assumes the output tensor shape is known (for example [1, 9, 8400]).
     */
    fun detect(tensorImage: TensorImage): FloatArray {
        // Create input buffer from tensorImage
        val inputBuffer = tensorImage.buffer
        val outputTensor = interpreter?.getOutputTensor(0)
            ?: throw IllegalStateException("Output tensor is null")
        // Define output shape. Here we assume output is [1, 9, 8400]
        val outputShape = outputTensor.shape()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)

        // Run inference
        interpreter?.run(inputBuffer, outputBuffer.buffer)
        return outputBuffer.floatArray
    }
}
