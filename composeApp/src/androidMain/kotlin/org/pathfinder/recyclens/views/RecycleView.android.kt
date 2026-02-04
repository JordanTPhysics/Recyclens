package org.pathfinder.recyclens.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.pathfinder.recyclens.SharedImageViewModel
import android.graphics.BitmapFactory
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.pathfinder.recyclens.ml.YoloDetector
import org.pathfinder.recyclens.ml.decodeDetections
import org.pathfinder.recyclens.ml.drawDetections
import org.pathfinder.recyclens.ml.preprocessImage
import org.pathfinder.recyclens.network.fetchRecycleResponse
import org.pathfinder.recyclens.ui.theme.LocalAppColors



@Composable
fun inferImage(bitmap: Bitmap): ImageBitmap {
    val detector = YoloDetector(LocalContext.current)
    detector.setup()

    val tensorImage = preprocessImage(bitmap, 320, 320)
    val results = detector.detect(tensorImage)
    val detectedObjects = decodeDetections(results, 8400, 9)
    return drawDetections(bitmap, detectedObjects).asImageBitmap()
}

fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

@Composable
actual fun RecycleView(
    onBack: () -> Unit,
    sharedImageViewModel: SharedImageViewModel
) {

    val image = byteArrayToBitmap(sharedImageViewModel.getImage()!!)

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {

        Image(
            bitmap = inferImage(image),
            contentDescription = "Recycle Image",
            modifier = Modifier
                .weight(3f)
                .border(10.dp, LocalAppColors.current.foreground)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
            ,

        ) {

            Button(
                onClick = {
//                    fetchRecycleResponse(image)
                          },
                modifier = Modifier
                    .scale(1.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalAppColors.current.foreground,
                    contentColor = LocalAppColors.current.background
                )

            ) {
                Text("Recycle")
            }
            Button(
                onClick = onBack,
                modifier = Modifier
                    .scale(1.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalAppColors.current.foreground,
                    contentColor = LocalAppColors.current.background
                )
            ) {
                Text("Discard")
            }
        }
    }
}


