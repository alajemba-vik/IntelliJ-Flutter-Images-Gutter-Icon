package com.alaje.learn.hb_flutter_image_gutter_viewer


import com.github.weisj.jsvg.nodes.SVG
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.Gray
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil
import com.intellij.util.IconUtil.createImageIcon
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.UIUtil
import com.kitfox.svg.SVGCache
import com.kitfox.svg.app.beans.SVGIcon
import java.awt.Component
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.Icon
import kotlin.math.min
import kotlin.math.sqrt


/**
 * Static utilities for generating scaled-down [Icon] instances from image resources to display in the gutter.
 */
internal object GutterIconFactory {
    private val LOG = Logger.getInstance(
        GutterIconFactory::class.java
    )

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
        maxHeight: Int
    ): Icon? {
        val path = file.path
        val icon: Icon? = if (path.endsWith(".svg")) {
            createSvgIcon(file, maxWidth = maxWidth, maxHeight = maxHeight)
        } else {
            createBitmapIcon(file, maxWidth, maxHeight)
        }

        if (icon == null) {
            println(String.format("Could not read icon image %1\$s", file.presentableUrl))
        }
        return  icon
    }


    private fun createSvgIcon(
       file: VirtualFile,
       maxWidth: Int,
       maxHeight: Int
    ): Icon? {

        try {

            val svgIcon = SVGIcon()
            svgIcon.svgURI = URI.create(file.url)
            svgIcon.preferredSize = Dimension(maxWidth, maxHeight)
            svgIcon.antiAlias = true
            svgIcon.isClipToViewbox = true

            val bufferedImage = IconUtil.toBufferedImage(svgIcon)

            return createBitmapIcon(bufferedImage, maxWidth, maxHeight)

        } catch (e: Exception) {
            // Not just IOExceptions here; for example, we've seen
            //IllegalArgumentException @ ...... < PNGImageReader:1479 < ... ImageIO.read

            LOG.error(String.format("Could not read svg image %1\$s", file.presentableUrl), e)
            return null
        }
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
