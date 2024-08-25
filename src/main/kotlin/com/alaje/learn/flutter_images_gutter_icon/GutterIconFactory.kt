package com.alaje.learn.flutter_images_gutter_icon


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


private val SVG_MAX_SIZE = JBUI.scale(18)
private val IMG_MAX_SIZE = JBUI.scale(18)
private val GIF_MAX_SIZE = JBUI.scale(30)


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


            // TODO - set fill color to none when fill color is transparent
            /*try {
            val diagram = svgIcon.svgUniverse.getDiagram(svgIcon.svgURI)
                val style = StyleAttribute()
                style.setName("fill")
                *//*
                val m = mutableListOf<SVGElement>()
                diagram.root.getChildren(m)
                m.forEach {
                    val attrs = it.presentationAttributes
                }

                m.first().presentationAttributes
                diagram.root.getStyle(style)
                *//*
                val element = diagram.getElement("path");
                element.getPres(style);

                //val value = style.stringValue

                val value = style.intList
                println("name color: ${file.name}")
                println("XXXFill color: $value")
                *//*diagram.root.setAttribute(
                    "fill", AnimationElement.AT_XML, "none"
                )*//*
            } catch (e: Exception) {
                LOG.error("Could not set fill color", e)
            }*/


            svgIcon.isClipToViewbox = true
            svgIcon.antiAlias = true

            val scale = min(maxWidth / svgIcon.iconWidth.toDouble(), maxHeight / svgIcon.iconHeight.toDouble())

            // ScaleContext.create(Scale(scale, ScaleType.OBJ_SCALE))
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
            LOG.error(String.format("Could not read icon image %1\$s", file.presentableUrl), e)
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
                    scale = min(
                        maxWidth / image.getWidth(null).toDouble(),
                        maxHeight / image.getHeight(null).toDouble()
                    )
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
