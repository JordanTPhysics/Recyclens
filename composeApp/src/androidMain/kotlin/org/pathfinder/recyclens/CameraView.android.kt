package org.pathfinder.recyclens

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.pathfinder.recyclens.interfaces.CameraController
import org.pathfinder.recyclens.shared.CameraMode
import org.pathfinder.recyclens.ui.theme.LocalAppColors

@Composable
actual fun CameraView(controller: CameraController) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        if(controller is AndroidCameraController) {
            controller.startCamera(previewView)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
        Box {

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
                update = {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val cameraSelector =
                        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                })
            var cameraMode by remember { mutableStateOf(CameraMode.Recycle) }
            CameraModeSlider(
                currentMode = 1,
                onModeChange = { newMode ->
                    controller.setMode(newMode)
                },
            )
            Button(
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                onClick = {
                    controller.takePicture { image ->
                        when (cameraMode) {
                            CameraMode.Recycle -> handleRecycleImage(image)
                            CameraMode.Report -> handleReportImage(image)
                        }
                    }
                }
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = Color.hsl(150f, 0.52f, 0.51f),
                        radius = size.minDimension / 3,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
            }
        }
    }
}


@Composable
fun CameraModeSlider(
    currentMode: Int,
    onModeChange: (Int) -> Unit,
) {
    val sliderValue = remember { mutableStateOf(currentMode.toFloat()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.background)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        Text("♻\uFE0F", style = MaterialTheme.typography.labelLarge)
        Text("⚠\uFE0F", style = MaterialTheme.typography.labelLarge)

        Slider(
            value = sliderValue.value,
            onValueChange = { newValue ->
                sliderValue.value = newValue
                onModeChange(newValue.toInt())  // Callback when value changes
            },
            valueRange = 1f..2f,  // Two discrete values
            steps = 0,  // Only allow 1 step between 1x and 2x
            colors = SliderDefaults.colors(
                thumbColor = LocalAppColors.current.primary,
                activeTrackColor = LocalAppColors.current.primary,
                inactiveTrackColor = LocalAppColors.current.border
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Dummy Functions for Different Image Handling
fun handleRecycleImage(image: ByteArray) {
    // Process for recycling (e.g., send to an AI model for classification)
}

fun handleReportImage(image: ByteArray) {
    // Process for reporting (e.g., send to a server or save for reports)
}



