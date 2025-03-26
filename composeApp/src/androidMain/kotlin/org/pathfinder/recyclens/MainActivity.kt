package org.pathfinder.recyclens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {
    private val cameraPermission = Manifest.permission.CAMERA
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            requestPermissionLauncher.launch(cameraPermission)
                App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}