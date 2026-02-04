package org.pathfinder.recyclens.models

import kotlinx.serialization.Serializable

@Serializable
data class RecycleImageResponse(
    val id: Int,
    val name: String,
    val description: String
)