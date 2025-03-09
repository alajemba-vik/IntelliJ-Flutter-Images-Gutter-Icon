package com.alaje.intellijplugins.flutter_images_gutter_icon.data

import com.alaje.intellijplugins.flutter_images_gutter_icon.utils.GutterIconUtils
import com.google.common.collect.Maps
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.UIUtil
import javax.swing.Icon
import kotlin.properties.Delegates

/**
 * A service class that manages the caching of gutter icons for files in a project.
 * This class ensures that icons are rendered and cached efficiently, taking into account
 * high DPI displays and file modifications.
 * */
@Service(Service.Level.PROJECT)
class GutterIconCache {
    /**
     * A concurrent map that stores the cached icons with their associated file paths as keys.
     * The cache helps in reusing already rendered icons, improving performance by avoiding
     * redundant icon rendering operations.
     */
    private val thumbnailCache: MutableMap<String, TimestampedIcon> = Maps.newConcurrentMap()

    /**
     * A property that indicates whether the display is high DPI. It is observed for changes,
     * and if the display type changes, the cache is cleared to ensure icons are rendered
     * appropriately for the new display type. Not doing so means that icons rendered once display settings
     * change to a high DPI display will be blurry and inconsistent
     */
    private var isHighDpiDisplay by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) thumbnailCache.clear()
    }

    private val fileDocManager by lazy{ FileDocumentManager.getInstance() }

    /**
     * Returns the potentially cached [Icon] rendered from the [file], or `null` if none could be
     * rendered.
     */
    fun getIcon(file: VirtualFile): Icon? {
        return (getTimestampedIconFromCache(file) ?: createAndCacheIcon(file)).icon
    }

    /**
    * Creates the icon using the [GutterIconUtils] and caches it for future accesses.
    * */
    private fun createAndCacheIcon(
        file: VirtualFile,
    ): TimestampedIcon {
        val icon = GutterIconUtils.createIcon(file)

        return TimestampedIcon(icon, file.modificationStamp).also {
            thumbnailCache[file.path] = it
        }
    }

    /**
     * Returns the [Icon] for the associated [file] if it is already stored in the cache,
     * otherwise `null`.
     */
    private fun getTimestampedIconFromCache(file: VirtualFile): TimestampedIcon? {
        isHighDpiDisplay = UIUtil.isRetina()

        val icon = thumbnailCache[file.path]?.takeIf { it.hasNotBeenModified(file, fileDocManager) }
        return icon
    }

    companion object {
        @JvmStatic fun getInstance(project: Project): GutterIconCache = project.service()
    }

}

private data class TimestampedIcon(val icon: Icon?, val timestamp: Long) {

    fun hasNotBeenModified(file: VirtualFile, fileDocManager: FileDocumentManager): Boolean {
        return timestamp == file.modificationStamp && !fileDocManager.isFileModified(file)
    }

}