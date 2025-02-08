package com.alaje.intellijplugins.flutter_images_gutter_icon


import com.alaje.intellijplugins.flutter_images_gutter_icon.utils.GutterIconCache.Companion.getInstance
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.*
import javax.swing.Icon

val defaultIcon = AllIcons.Ide.FatalError

class FlutterGutterImageIconRenderer(
    file: VirtualFile?,
    private val project: Project
) : com.intellij.openapi.editor.markup.GutterIconRenderer(), DumbAware {
    private val myFile = file
    override fun getIcon(): Icon {
        val icon = if (myFile != null) getInstance(project).getIcon(myFile) else defaultIcon
        return icon ?: defaultIcon
    }

    override fun getClickAction(): AnAction {
        return GutterIconClickAction(myFile)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as FlutterGutterImageIconRenderer

        return myFile == that.myFile
    }

    override fun hashCode(): Int {
        return Objects.hashCode(myFile)
    }

    private inner class GutterIconClickAction(
        private val myFile: VirtualFile?
    ) : AnAction(), NavigationTargetProvider {
        private var myNavigationTarget: VirtualFile? = null
        private var myNavigationTargetComputed = false

        override fun actionPerformed(event: AnActionEvent) {
            val openFileDescriptor = OpenFileDescriptor(project, navigationTarget!!)
            openFileDescriptor.navigate(true)
        }

        override val navigationTarget: VirtualFile?
            get() {
                if (!myNavigationTargetComputed && myFile != null) {
                    myNavigationTarget = myFile
                    myNavigationTargetComputed = true
                }
                return myNavigationTarget
            }
    }

    interface NavigationTargetProvider {
        val navigationTarget: VirtualFile?
    }
}
