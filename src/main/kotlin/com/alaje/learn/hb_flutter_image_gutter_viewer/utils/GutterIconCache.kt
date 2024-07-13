package com.alaje.learn.hb_flutter_image_gutter_viewer.utils


import com.alaje.learn.hb_flutter_image_gutter_viewer.BaseHBImageResourceExternalAnnotator.FileAnnotationInfo
import com.alaje.learn.hb_flutter_image_gutter_viewer.GutterIconFactory
import com.google.common.collect.Maps
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.TestOnly
import javax.swing.Icon
import kotlin.properties.Delegates.observable


private val MAX_WIDTH = JBUI.scale(16)
private val MAX_HEIGHT = JBUI.scale(16)

private fun defaultRenderIcon(
    file: VirtualFile,
) = GutterIconFactory.createIcon(file, MAX_WIDTH, MAX_HEIGHT)

@Service(Service.Level.PROJECT)
class GutterIconCache
@TestOnly
constructor(
    private val highDpiSupplier: () -> Boolean,
    private val renderIcon: (VirtualFile) -> Icon?
) {
    private val thumbnailCache: MutableMap<String, TimestampedIcon> = Maps.newConcurrentMap()
    private var highDpiDisplay by
    observable(false) { _, oldValue, newValue -> if (oldValue != newValue) thumbnailCache.clear() }

    constructor() : this(
        UIUtil::isRetina,
        {vf -> defaultRenderIcon(vf)}
    )

    /**
     * Returns the potentially cached [Icon] rendered from the [file], or `null` if none could be
     * rendered.
     */
    fun getIcon(file: VirtualFile): Icon? =
        (getTimestampedIconFromCache(file) ?: renderAndCacheIcon(file)).icon

    private fun renderAndCacheIcon(
        file: VirtualFile,
    ): TimestampedIcon =
        TimestampedIcon(renderIcon(file), file.modificationStamp).also {
            thumbnailCache[file.path] = it
        }

    /**
     * Returns the [Icon] for the associated [file] if it is already rendered and stored in the cache,
     * otherwise `null`.
     */
    private fun getTimestampedIconFromCache(file: VirtualFile): TimestampedIcon? {
        highDpiDisplay = highDpiSupplier()
        return thumbnailCache[file.path]?.takeIf { it.isAsNewAs(file) }
    }

    data class TimestampedIcon(val icon: Icon?, val timestamp: Long) {
        fun isAsNewAs(file: VirtualFile) =
            timestamp == file.modificationStamp && !FileDocumentManager.getInstance().isFileModified(file)
    }

    fun renderIcon(
        annotationResult: Map<FileAnnotationInfo.AnnotatableElement, GutterIconRenderer>?,
        holder: AnnotationHolder
    ) {
        annotationResult?.forEach { (k: FileAnnotationInfo.AnnotatableElement, v: GutterIconRenderer?) ->
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