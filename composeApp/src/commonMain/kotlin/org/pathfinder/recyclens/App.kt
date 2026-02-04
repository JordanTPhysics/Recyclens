package org.pathfinder.recyclens


import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.pathfinder.recyclens.navigation.AppRouter
import org.pathfinder.recyclens.ui.theme.ComposeAppTheme

@Preview
@Composable
fun App() {
    val sharedImageViewModel = remember { SharedImageViewModel() }
    ComposeAppTheme {
        AppRouter(sharedImageViewModel)
    }
}