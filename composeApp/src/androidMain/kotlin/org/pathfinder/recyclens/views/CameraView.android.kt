@file:OptIn(ExperimentalMaterial3Api::class)

package org.pathfinder.recyclens.views

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.pathfinder.recyclens.AndroidCameraController
import org.pathfinder.recyclens.R
import org.pathfinder.recyclens.SharedImageViewModel
import org.pathfinder.recyclens.interfaces.CameraController
import org.pathfinder.recyclens.ml.DetectionResult
import org.pathfinder.recyclens.ui.theme.ColorPalette
import org.pathfinder.recyclens.ui.theme.LocalAppColors

@Composable
fun getColors(): ColorPalette {
    return LocalAppColors.current
}


@Composable
actual fun CameraView(
    controller: CameraController,
    onNavigateToRecycle: () -> Unit,
    onNavigateToReport: () -> Unit,
    sharedImageViewModel: SharedImageViewModel
    ) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val predictions = remember { mutableStateListOf<DetectionResult>() }

    LaunchedEffect(Unit) {
        if(controller is AndroidCameraController) {
            controller.startCamera(previewView) { newPredictions ->
                predictions.clear()
                predictions.addAll(newPredictions)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
        Box {

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector =  CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                })
            Canvas(modifier = Modifier.fillMaxSize()) {
                predictions.forEach { detection ->
                    // Assume boundingBox values are normalized (0..1) and DetectionResult has a property 'boundingBox: RectF'
                    val left = detection.boundingBox.left * size.width
                    val top = detection.boundingBox.top * size.height
                    val right = detection.boundingBox.right * size.width
                    val bottom = detection.boundingBox.bottom * size.height

                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    // Optionally draw the label or confidence:
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "${detection.classId} (${(detection.confidence * 100).toInt()}%)",
                            left,
                            top - 8.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 16.dp.toPx()
                            }
                        )
                    }
                }
            }
            var cameraModeReport by remember { mutableStateOf(false) }

            Switch(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .scale(2f)
                    .offset(y = 16.dp)
                ,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Transparent,
                    uncheckedThumbColor = Color.Transparent,
                    checkedTrackColor = Color.Transparent,
                    uncheckedTrackColor = Color.Transparent,
                    checkedBorderColor = Color.hsl(211f, 0.16f, 0.51f, 0.5f),
                    uncheckedBorderColor = Color.hsl(211f, 0.16f, 0.51f, 0.5f)
                ),
                checked = cameraModeReport,
                onCheckedChange = {
                    cameraModeReport = it
                },

                thumbContent = {
                    Icon(
                        painter = painterResource(id = if(cameraModeReport) R.drawable.report_24px else R.drawable.recycling_24px),
                        contentDescription = if(cameraModeReport) "Report" else "Recycle",
                        tint = if(cameraModeReport) getColors().warning else getColors().primary)

                }
            )

            Button(
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomCenter)
                    .background(Color.Transparent),
                onClick = {
                    controller.takePicture { image ->
                        if (cameraModeReport) {
                            handleReportImage(image, sharedImageViewModel, onNavigateToReport)
                        } else
                        {
                            handleRecycleImage(image, sharedImageViewModel, onNavigateToRecycle)
                        }
                    }
                }
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = Color.Magenta,
                        radius = size.minDimension / 3,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
            }
        }
    }
}

fun handleRecycleImage(image: ByteArray, sharedImageViewModel: SharedImageViewModel, onNavigateToRecycle: () -> Unit) {
    sharedImageViewModel.setImage(image)
    onNavigateToRecycle()
}

fun handleReportImage(image: ByteArray, sharedImageViewModel: SharedImageViewModel, onNavigateToReport: () -> Unit) {
    sharedImageViewModel.setImage(image)
    onNavigateToReport()
    }

