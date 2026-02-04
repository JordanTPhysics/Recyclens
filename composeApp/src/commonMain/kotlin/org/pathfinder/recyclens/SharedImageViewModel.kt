package org.pathfinder.recyclens

import androidx.compose.runtime.mutableStateOf

class SharedImageViewModel {
    private val _image = mutableStateOf<ByteArray?>(null)

    fun setImage(byteArray: ByteArray) {
        _image.value = byteArray
    }

    fun getImage(): ByteArray? {
        return _image.value
    }


}