package com.alaje.intellijplugins.flutter_images_gutter_icon

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange

// Data class to hold the file annotation information
class ImageIconAnnotationInfo(
    val editor: Editor,
) {
    val timestamp: Long = editor.document.modificationStamp
    val elements: ArrayList<AnnotatableElement> = ArrayList()


    data class AnnotatableElement(
        val image: String,
        val textRange: TextRange
    )
}
