package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.BaseHBImageResourceExternalAnnotator.FileAnnotationInfo
import com.alaje.learn.hb_flutter_image_gutter_viewer.utils.GutterIconCache
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.LineMarkerSettings
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Base class for external annotators that place resource icons in the gutter of the editor.
 */
abstract class BaseHBImageResourceExternalAnnotator
    : ExternalAnnotator<
        FileAnnotationInfo?,
        Map<FileAnnotationInfo.AnnotatableElement, GutterIconRenderer>?>()
{
    private val lineMarkerProvider = LineMarkerProvider()


    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): FileAnnotationInfo? {
        if (!LineMarkerSettings.getSettings().isEnabled(lineMarkerProvider)) {
            return null
        }

        return collectInformation(file, editor, file.virtualFile.path)
    }

    protected abstract fun collectInformation(file: PsiFile, editor: Editor, imagePath: String): FileAnnotationInfo?

    override fun doAnnotate(collectedInfo: FileAnnotationInfo?): Map<FileAnnotationInfo.AnnotatableElement, GutterIconRenderer>? {
        val editor = collectedInfo?.editor ?: return null
        val project = editor.project ?: return null
        val timestamp = collectedInfo.timestamp
        val document = editor.document

        val rendererMap: MutableMap<FileAnnotationInfo.AnnotatableElement, GutterIconRenderer> = HashMap()

        for (element in (collectedInfo.elements)) {
            ProgressManager.checkCanceled()
            if (editor.isDisposed || (document.modificationStamp) > timestamp) {
                return null
            }

            if (LOG.isDebugEnabled) {
                LOG.debug(
                    String.format(
                        "Rendering icon for %s in %s.",
                        collectedInfo.file
                    )
                )
            }

            val gutterIconRenderer: GutterIconRenderer? = getResourceGutterIconRenderer(
                project,
                element.image
            )
            if (gutterIconRenderer != null) {
                rendererMap[element] = gutterIconRenderer
            }
        }
        return rendererMap
    }


    override fun apply(
        file: PsiFile,
        annotationResult: Map<FileAnnotationInfo.AnnotatableElement, GutterIconRenderer>?,
        holder: AnnotationHolder
    ) {
        GutterIconCache.getInstance(file.project).renderIcon(annotationResult, holder)
    }

    override fun isDumbAware(): Boolean {
        return true
    }

    class FileAnnotationInfo(
        val file: PsiFile,
        val editor: Editor,
    ) {
        val timestamp: Long = editor.document.modificationStamp
        val elements: ArrayList<AnnotatableElement> = ArrayList()


        data class AnnotatableElement(
            val image: String,
            val textRange: TextRange
        )
    }

    /**
     * Provider used to enable/disable Android resource gutter icons.
     *
     *
     * This provider doesn't directly provide any of the resource gutter icons; that's done by subclasses of
     * [BaseHBImageResourceExternalAnnotator]. But since those are [ExternalAnnotator]s, they don't show up in Gutter icon
     * settings. This provider does show up in settings, and the other annotators check its value to determine if they should be enabled.
     */
    class LineMarkerProvider : LineMarkerProviderDescriptor() {
        override fun getName(): String {
            return "Resource preview"
        }

        override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
            return null
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            BaseHBImageResourceExternalAnnotator::class.java
        )

        private fun getResourceGutterIconRenderer(
            project: Project,
            imagePath: String,
        ): GutterIconRenderer? {
            val resourceFile = LocalFileSystem.getInstance().findFileByPath(imagePath) ?: return null
            // Updating the GutterIconCache in the background thread to include the icon.
            GutterIconCache.getInstance(project).getIcon(resourceFile)
            return HBFlutterGutterImageIconRenderer(resourceFile,project)
        }

    }
}