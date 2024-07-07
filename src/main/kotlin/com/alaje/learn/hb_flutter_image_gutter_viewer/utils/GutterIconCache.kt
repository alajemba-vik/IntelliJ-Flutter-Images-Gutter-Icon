package com.alaje.learn.hb_flutter_image_gutter_viewer.utils

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.alaje.learn.hb_flutter_image_gutter_viewer.GutterIconFactory
import com.google.common.collect.Maps
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.Icon
import kotlin.properties.Delegates.observable
import org.jetbrains.annotations.TestOnly

private fun defaultRenderIcon(
    file: VirtualFile,
    project: Project
) = GutterIconFactory.createIcon(file, JBUI.scale(16), JBUI.scale(16), project)

@Service(Service.Level.PROJECT)
class GutterIconCache
@TestOnly
constructor(
    private val project: Project,
    private val highDpiSupplier: () -> Boolean,
    private val renderIcon: (VirtualFile) -> Icon?
) {
    private val thumbnailCache: MutableMap<String, TimestampedIcon> = Maps.newConcurrentMap()
    private var highDpiDisplay by
    observable(false) { _, oldValue, newValue -> if (oldValue != newValue) thumbnailCache.clear() }

    constructor(project: Project) : this(
        project,
        UIUtil::isRetina,
        {vf -> defaultRenderIcon(vf, project)}
    )

    /**
     * Returns the potentially cached [Icon] rendered from the [file], or `null` if none could be
     * rendered.
     */
    fun getIcon(file: VirtualFile): Icon? =
        (getTimestampedIconFromCache(file) ?: renderAndCacheIcon(file)).icon

    /**
     * Returns the [Icon] for the associated [file] if it is already rendered and stored in the cache,
     * otherwise `null`.
     */
    fun getIconIfCached(file: VirtualFile): Icon? = getTimestampedIconFromCache(file)?.icon

    private fun renderAndCacheIcon(
        file: VirtualFile,
    ): TimestampedIcon =
        TimestampedIcon(renderIcon(file), file.modificationStamp).also {
            thumbnailCache[file.path] = it
        }

    private fun getTimestampedIconFromCache(file: VirtualFile): TimestampedIcon? {
        highDpiDisplay = highDpiSupplier()
        return thumbnailCache[file.path]?.takeIf { it.isAsNewAs(file) }
    }

    data class TimestampedIcon(val icon: Icon?, val timestamp: Long) {
        fun isAsNewAs(file: VirtualFile) =
            timestamp == file.modificationStamp && !FileDocumentManager.getInstance().isFileModified(file)
    }

    companion object {
        @JvmStatic fun getInstance(project: Project): GutterIconCache = project.service()
    }
}