package com.alaje.intellijplugins.flutter_images_gutter_icon.utils


import com.alaje.intellijplugins.flutter_images_gutter_icon.BaseHBImageResourceExternalAnnotator.FileAnnotationInfo
import com.alaje.intellijplugins.flutter_images_gutter_icon.GutterIconFactory
import com.google.common.collect.Maps
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.UIUtil
import javax.swing.Icon
import kotlin.properties.Delegates.observable



private fun defaultRenderIcon(
    file: VirtualFile,
) = GutterIconFactory.createIcon(file)

@Service(Service.Level.PROJECT)
class GutterIconCache(
    private val highDpiSupplier: () -> Boolean,
    private val renderIcon: (VirtualFile) -> Icon?
) {
    private val thumbnailCache: MutableMap<String, TimestampedIcon> = Maps.newConcurrentMap()
    private var highDpiDisplay by observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) thumbnailCache.clear()
    }

    constructor() : this(
        UIUtil::isRetina,
        {vf -> defaultRenderIcon(vf)}
    )

    /**
     * Returns the potentially cached [Icon] rendered from the [file], or `null` if none could be
     * rendered.
     */
    fun getIcon(file: VirtualFile): Icon? {
        return (getTimestampedIconFromCache(file) ?: renderAndCacheIcon(file)).icon
    }

    private fun renderAndCacheIcon(
        file: VirtualFile,
    ): TimestampedIcon {
        return TimestampedIcon(renderIcon(file), file.modificationStamp).also {
            thumbnailCache[file.path] = it
        }
    }

    /**
     * Returns the [Icon] for the associated [file] if it is already rendered and stored in the cache,
     * otherwise `null`.
     */
    private fun getTimestampedIconFromCache(file: VirtualFile): TimestampedIcon? {
        highDpiDisplay = highDpiSupplier()
        val icon = thumbnailCache[file.path]?.takeIf { it.hasNotBeenModified(file) }

        return icon
    }

    data class TimestampedIcon(val icon: Icon?, val timestamp: Long) {
        fun hasNotBeenModified(file: VirtualFile): Boolean {
            return timestamp == file.modificationStamp && !FileDocumentManager.getInstance().isFileModified(file)
        }
    }

    fun renderIcon(
        annotationResult: Map<FileAnnotationInfo.AnnotatableElement, GutterIconRenderer>?,
        holder: AnnotationHolder
    ) {
        annotationResult?.forEach { (k: FileAnnotationInfo.AnnotatableElement, v: GutterIconRenderer) ->
            // show image in gutter icon
            holder.newSilentAnnotation(
                HighlightSeverity.INFORMATION
            ).range(k.textRange).gutterIconRenderer(v).create()
        }
    }

    companion object {
        @JvmStatic fun getInstance(project: Project): GutterIconCache = project.service()
    }
}