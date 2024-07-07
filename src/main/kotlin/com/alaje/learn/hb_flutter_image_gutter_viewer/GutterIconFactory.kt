package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.android.ide.common.vectordrawable.VdPreview
import com.android.tools.idea.rendering.DrawableRenderer
import com.android.utils.XmlUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.Gray
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil.createImageIcon
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.util.projectStructure.getModule
import java.awt.Dimension
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.Icon
import kotlin.math.min


/**
 * Static utilities for generating scaled-down [Icon] instances from image resources to display in the gutter.
 */
internal object GutterIconFactory {
    private val LOG = Logger.getInstance(
        GutterIconFactory::class.java
    )
    private const val RENDERING_SCALING_FACTOR = 10

    /**
     * Given the path to an image resource, returns an Icon which displays the image, scaled so that its width
     * and height do not exceed `maxWidth` and `maxHeight` pixels, respectively. Returns null if unable to read
     * or render the image resource for any reason.
     *
     *
     * For an XML resource, `resolver` is used to resolve resource and theme references (e.g. `@string/foo` , `?android:attr/bar`). If `resolver` is null, then it is assumed that the image resource is either not an XML resource, or
     * that the XML file does not contain any unresolved references (otherwise, this method returns null).
     */
    fun createIcon(
        file: VirtualFile,
        maxWidth: Int,
        maxHeight: Int,
        project: Project
    ): Icon? {
        val path = file.path
        if (path.endsWith(".svg")) {
            return createSvgIcon(file, maxWidth = maxWidth, maxHeight = maxHeight, project)
        }
        return createBitmapIcon(file, maxWidth, maxHeight)
    }

    @Throws(IOException::class)
    private fun getXmlContent(file: VirtualFile): String {
        val document = FileDocumentManager.getInstance().getCachedDocument(file)
            ?: return String(file.contentsToByteArray())

        return document.text
    }

    private fun createSvgIcon(
       file: VirtualFile,
       maxWidth: Int, maxHeight: Int,
         project: Project
    ): Icon? {
        try {

            val xml: String = getXmlContent(file)
            var image: Image?
            // If drawable is a vector drawable, use the renderer inside Studio.
            // Otherwise, delegate to layoutlib.
            if (xml.contains("<vector")) {
                val imageTargetSize: VdPreview.TargetSize =
                    VdPreview.TargetSize.createFromMaxDimension(JBUI.pixScale(maxWidth.toFloat()).toInt())
                val document: org.w3c.dom.Document = XmlUtils.parseDocumentSilently(xml, true) ?: return null

                val builder = StringBuilder(100)
                image = VdPreview.getPreviewFromVectorDocument(imageTargetSize, document, builder)
                image = ImageUtil.ensureHiDPI(image, ScaleContext.create())
                if (builder.isNotEmpty()) {
                    LOG.warn("Problems rendering " + file.presentableUrl + ": " + builder)
                }

            } else {

                val facet = AndroidFacet.getInstance(file.getModule(project =project) ?: return null) ?: return null
                val renderer = DrawableRenderer(facet)
                val size = Dimension(maxWidth * RENDERING_SCALING_FACTOR, maxHeight * RENDERING_SCALING_FACTOR)
                try {
                    val imageFuture: CompletableFuture<BufferedImage> = renderer.renderDrawable(xml, size)
                    // TODO(http://b/143455172): Remove the timeout by removing usages of this method on the UI thread. For now we just ensure
                    //  we do not block indefinitely on the UI thread. We also do not use the timeout in unit test to avoid non deterministic tests.
                    //  On production, if the request times out, it will cause the icon on the gutter not to show which is an acceptable fallback
                    //  until this is correctly fixed.
                    //  250ms should be enough time for inflating and rendering and is used a upper boundary.
                    //
                    // When running in the background thread, we wait for the future to complete indefinitely. If this call happens within a
                    // non-blocking read action, awaitWithCheckCanceled will allow write actions to cancel the wait. This avoids this thread
                    // holding the lock and causing dead-locks.
                    image = if (ApplicationManager.getApplication().isDispatchThread && !ApplicationManager.getApplication().isUnitTestMode)
                            imageFuture[250, TimeUnit.MILLISECONDS]
                    else ProgressIndicatorUtils.awaitWithCheckCanceled(imageFuture)
                } catch (e: Throwable) {
                    // If an invalid drawable is passed, renderDrawable might throw an exception. We can not fully control the input passed to this
                    // rendering call since the user might be referencing an invalid drawable, so we are just less verbose about it. The user will
                    // not see the preview next to the code when referencing invalid drawables.
                    val message = String.format("Could not read/render icon image %1\$s", file.presentableUrl)
                    if (ApplicationManager.getApplication().isUnitTestMode) {
                        LOG.error(message, e)
                    } else {
                        LOG.debug(message, e)
                    }
                    image = null
                } finally {
                    Disposer.dispose(renderer)
                }
                if (image == null) {
                    return null
                }
                image = ImageUtil.ensureHiDPI(image, ScaleContext.create())
                image = ImageUtil.scaleImage(image, maxWidth, maxHeight)
            }

            return createImageIcon(image ?: return null)
        } catch (e: Throwable) {
            val message = String.format("Could not read/render icon image %1\$s", file.presentableUrl)
            if (ApplicationManager.getApplication().isUnitTestMode) {
                LOG.error(message, e)
            } else {
                LOG.warn(message, e)
            }
        }

        return null
    }

    private fun createBitmapIcon(file: VirtualFile, maxWidth: Int, maxHeight: Int): Icon? {

        try {
            file.inputStream.use { stream ->
                return createBitmapIcon(ImageIO.read(stream), maxWidth, maxHeight)
            }
        } catch (e: Exception) {
            // Not just IOExceptions here; for example, we've seen
            // IllegalArgumentException @ ...... < PNGImageReader:1479 < ... ImageIO.read

            LOG.warn(String.format("Could not read icon image %1\$s", file.presentableUrl), e)
            return null
        }
    }

    private fun createBitmapIcon(bufferedImage: BufferedImage?, maxWidth: Int, maxHeight: Int): Icon? {
        if (bufferedImage != null) {
            var image = ImageUtil.ensureHiDPI(bufferedImage, ScaleContext.create())
            val imageWidth = image.getWidth(null)
            val imageHeight = image.getHeight(null)

            if (imageWidth > maxWidth || imageHeight > maxHeight) {
                var scale = min(maxWidth / imageWidth.toDouble(), maxHeight / imageHeight.toDouble())

                if (bufferedImage.type == BufferedImage.TYPE_BYTE_INDEXED) {
                    // Indexed images look terrible if they are scaled directly; instead, paint into an ARGB blank image
                    val bg = ImageUtil.createImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
                    val g = bg.graphics
                    g.color = Gray.TRANSPARENT
                    g.fillRect(0, 0, bg.width, bg.height)
                    UIUtil.drawImage(g, image, 0, 0, null)
                    g.dispose()
                    image = bg
                }

                image = ImageUtil.scaleImage(image, scale)
                // ImageUtil.scaleImage does not guarantee scaling for HiDPI images: in case scaling factor
                // is small enough for the resulting image to have 0 width or high, the original (unscaled)
                // image will be returned!
                if (image.getWidth(null) > maxWidth || image.getHeight(null) > maxHeight) {
                    image = ImageUtil.toBufferedImage(image, false)
                    // The scale might have changed since the underlying BufferedImage obtained from the HiDPI image
                    // in the previous line might have different size.
                    scale =
                        min(maxWidth / image.getWidth(null).toDouble(), maxHeight / image.getHeight(null).toDouble())
                    image = ImageUtil.scaleImage(image, scale)
                }
            } else {
                // If the image is smaller than the max size, simply use it as is instead of scaling down and then up.
                image = bufferedImage
            }


            return createImageIcon(image)
        }

        return null
    }
}