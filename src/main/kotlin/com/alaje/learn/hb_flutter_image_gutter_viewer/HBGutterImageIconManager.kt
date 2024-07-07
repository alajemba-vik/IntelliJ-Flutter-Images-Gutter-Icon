package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.utils.isFlutterFile
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object HBGutterImageIconManager {
    fun addGutterMark(editor: Editor, line: Int, containingFile: VirtualFile?, path: String) {

        if (!isFlutterFile(containingFile)) return
        val markupModel = editor.markupModel
        val highlighter = markupModel.addLineHighlighter(line, 0, null)

        highlighter.gutterIconRenderer = HBFlutterGutterImageIconRenderer(path)
    }



}