package com.alaje.intellijplugins.flutter_images_gutter_icon


import com.alaje.intellijplugins.flutter_images_gutter_icon.utils.GutterIconCache
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.*
import javax.swing.Icon


class GutterIconRenderer(
    private val imageFile: VirtualFile,
    private val project: Project
) : com.intellij.openapi.editor.markup.GutterIconRenderer(), DumbAware {

    val defaultIcon get() = AllIcons.Ide.FatalError

    private val gutterIconCache by lazy{ GutterIconCache.getInstance(project) }

    init {
        // Updating the GutterIconCache in the background thread to include the icon.
        gutterIconCache.getIcon(imageFile)
    }

    override fun getIcon(): Icon {
        return gutterIconCache.getIcon(imageFile) ?: defaultIcon
    }

    override fun getClickAction(): AnAction {
        return GutterIconClickAction(imageFile, project)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is GutterIconRenderer) return false

        return imageFile == other.imageFile
    }

    override fun hashCode(): Int {
        return Objects.hashCode(imageFile)
    }
}

private class GutterIconClickAction(
    private val imageFile: VirtualFile?,
    private val project: Project
) : AnAction() {
    private var lastComputedNavigationTarget: VirtualFile? = null

    override fun actionPerformed(event: AnActionEvent) {
        val openFileDescriptor = OpenFileDescriptor(project, navigationTarget ?: return)
        openFileDescriptor.navigate(true)
    }

    val navigationTarget: VirtualFile?
        get() {
            if (lastComputedNavigationTarget == null && imageFile != null) {
                lastComputedNavigationTarget = imageFile
            }
            return lastComputedNavigationTarget
        }
}

