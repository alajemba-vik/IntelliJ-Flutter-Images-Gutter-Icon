package com.alaje.intellijplugins.flutter_images_gutter_icon.utils


import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.Gray
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil
import com.intellij.util.IconUtil.createImageIcon
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.kitfox.svg.app.beans.SVGIcon
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.Icon
import kotlin.math.min


/**
 * A utility object with functions that help generate scaled-down [Icon] instances from image files to display in the gutter.
 */
object GutterIconUtils {
    private val LOG = Logger.getInstance(
        GutterIconUtils::class.java
    )

    /**
     * Returns an Icon using the path from the file, which displays the image, scaled so that its width
     * and height do not exceed `maxWidth` and `maxHeight` pixels, respectively.
     * Returns null if unable to read or render the image resource.
     **/
    fun createIcon(
        file: VirtualFile,
    ): Icon? {
        val path = file.path
        val icon: Icon? = if (path.endsWith(".svg")) {
            createSvgIcon(file, maxWidth = SVG_MAX_SIZE, maxHeight = SVG_MAX_SIZE)
        } else {
            createBitmapIcon(
                file,
                if (path.endsWith(".gif")) GIF_MAX_SIZE else IMG_MAX_SIZE,
                if (path.endsWith(".gif")) GIF_MAX_SIZE else IMG_MAX_SIZE
            )
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

            // TODO: Set fill color to none when fill color is transparent

            svgIcon.isClipToViewbox = true
            svgIcon.antiAlias = true

            val scale = min(maxWidth / svgIcon.iconWidth.toDouble(), maxHeight / svgIcon.iconHeight.toDouble())

            val icon = IconUtil.scale(
                svgIcon,
                null,
                scale.toFloat()
            )

            return icon

        } catch (e: Exception) {

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
            LOG.error(String.format("Could not read image %1\$s", file.presentableUrl), e)
            return null
        }
    }

    private fun createBitmapIcon(bufferedImage: BufferedImage?, maxWidth: Int, maxHeight: Int): Icon? {
        if (bufferedImage != null) {
            // Ensure the image is HiDPI compatible
            // This step is necessary to make sure the image looks sharp on high-resolution displays
            var image = ImageUtil.ensureHiDPI(bufferedImage, ScaleContext.create())
            val imageWidth = image.getWidth(null)
            val imageHeight = image.getHeight(null)

            // Check if the image dimensions exceed the maximum allowed size
            if (imageWidth > maxWidth || imageHeight > maxHeight) {
                // Calculate the scaling factor to fit the image within the maximum dimensions
                var scale = min(maxWidth / imageWidth.toDouble(), maxHeight / imageHeight.toDouble())

                if (bufferedImage.type == BufferedImage.TYPE_BYTE_INDEXED) {
                    // Indexed images look terrible if they are scaled directly; instead, paint into an ARGB blank image
                    val argbImage = ImageUtil.createImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
                    val imageGraphics = argbImage.graphics
                    imageGraphics.color = Gray.TRANSPARENT
                    imageGraphics.fillRect(0, 0, argbImage.width, argbImage.height)
                    UIUtil.drawImage(imageGraphics, image, 0, 0, null)
                    imageGraphics.dispose()
                    image = argbImage
                }

                // Scale the image to the calculated size
                image = ImageUtil.scaleImage(image, scale)
                // ImageUtil.scaleImage does not guarantee scaling for HiDPI images: in case scaling factor
                // is small enough for the resulting image to have 0 width or height, the original (unscaled)
                // image will be used to create the ImageIcon!
                if (image.getWidth(null) > maxWidth || image.getHeight(null) > maxHeight) {
                    // Convert the image to a BufferedImage, necessary to ensure that the image can be manipulated
                    // correctly
                    image = ImageUtil.toBufferedImage(image, false)
                    // Recalculate the scaling factor since the image size might have changed after transforming
                    // it to a BufferedImage
                    scale = min(
                        maxWidth / image.getWidth(null).toDouble(),
                        maxHeight / image.getHeight(null).toDouble()
                    )
                    // Scale the image again to fit within the maximum dimensions
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

private val SVG_MAX_SIZE = JBUI.scale(18)
private val IMG_MAX_SIZE = JBUI.scale(18)
private val GIF_MAX_SIZE = JBUI.scale(30)