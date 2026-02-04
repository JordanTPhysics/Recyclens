package org.pathfinder.recyclens.views

import androidx.compose.runtime.Composable
import org.pathfinder.recyclens.SharedImageViewModel

@Composable
expect fun ReportView(
    onBack: () -> Unit,
    sharedImageViewModel: SharedImageViewModel
)