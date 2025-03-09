package com.alaje.intellijplugins.flutter_images_gutter_icon.utils

object ImageUtils {
    fun String.hasImageFileExtension(): Boolean {
        return endsWith(".png", true) || endsWith(".jpg", true) || endsWith(".jpeg", true) ||
                endsWith(".gif", true) || endsWith(".bmp", true) || endsWith(".svg", true)
    }
}