package com.alaje.learn.hb_flutter_image_gutter_viewer


import com.alaje.learn.hb_flutter_image_gutter_viewer.utils.GutterIconCache.Companion.getInstance
import com.google.common.annotations.VisibleForTesting
import com.intellij.icons.AllIcons.Icons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import java.util.*
import javax.swing.Icon


class GutterIconRenderer(
    file: VirtualFile?,
    private val project: Project
) : com.intellij.openapi.editor.markup.GutterIconRenderer(), DumbAware {
    private val myFile = file
    override fun getIcon(): Icon {
        val icon = if (myFile != null
        ) getInstance(project).getIcon(myFile)
        else defaultIcon
        return icon ?: defaultIcon
    }

    override fun getClickAction(): AnAction? {
        return GutterIconClickAction(myFile)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as GutterIconRenderer

        if (myFile != that.myFile) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hashCode(myFile)
    }

    private inner class GutterIconClickAction(
        private val myFile: VirtualFile?
    ) :
        AnAction(), NavigationTargetProvider {
        private var myNavigationTarget: VirtualFile? = null
        private var myNavigationTargetComputed = false

        override fun actionPerformed(event: AnActionEvent) {
            //val editor = event.getData(CommonDataKeys.EDITOR) ?: return

            Messages.showInfoMessage("Clicked", "Click action")
            /*

            // Show the resource picker popup.
            ResourceChooserHelperKt.createAndShowResourcePickerPopup(
                ResourceType.DRAWABLE,
                myConfiguration,
                myFacet,
                pickerSources,
                MouseInfo.getPointerInfo().location
            ) { resourceReference ->
                setAttribute(resourceReference)
                null
            }*/
        }

        @get:VisibleForTesting
        override val navigationTarget: VirtualFile?
            get() {
                if (!myNavigationTargetComputed && myFile != null) {
                    myNavigationTarget = myFile
                    myNavigationTargetComputed = true
                }
                return myNavigationTarget
            }
    }

    @VisibleForTesting
    interface NavigationTargetProvider {
        val navigationTarget: VirtualFile?
    }

    companion object {
        private const val SET_RESOURCE_COMMAND_NAME = "Resource picked"
    }
}
