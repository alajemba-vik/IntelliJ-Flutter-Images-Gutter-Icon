package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.esotericsoftware.kryo.kryo5.minlog.Log
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.intellij.ui.Gray
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil.createImageIcon
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.jetbrains.lang.dart.DartTokenTypes
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.Icon
import kotlin.math.min

val defaultIcon = AllIcons.General.Warning
class HBFlutterGutterImageIconRenderer(private val path: String) : GutterIconRenderer(), DumbAware {

    override fun equals(other: Any?): Boolean {
        return other is HBFlutterGutterImageIconRenderer
    }

    override fun hashCode(): Int {
        return icon.hashCode()
    }


    override fun getIcon(): Icon {
        return createIcon(
            path,
            MAX_WIDTH,
            MAX_HEIGHT
        ) ?: defaultIcon
    }


    override fun getTooltipText(): String {
        return "This is a GutterMark!"
    }

    /**
     * Given the path to an image resource, returns an Icon which displays the image, scaled so that its width
     * and height do not exceed {@code maxWidth} and {@code maxHeight} pixels, respectively. Returns null if unable to read
     * or render the image resource for any reason.
     */
    private fun createIcon(
        imagePath: String,
        maxWidth: Int,
        maxHeight: Int
    ): Icon? {
        val file = LocalFileSystem.getInstance().findFileByPath(imagePath)  ?: return null
        return createBitmapIcon(file, maxWidth, maxHeight)
    }

    private fun createBitmapIcon(file: VirtualFile, maxWidth: Int, maxHeight: Int): Icon? {

       /* launch(Dispatchers.EDT) {
            val uiData = collectUiData()
            // switch to Default:
            val result = withContext(Dispatchers.Default) {
                compute(uiData)
            }
            // this will be resumed on EDT automatically:
            applyUiData(result)
        }*/

        try {
            file.inputStream.use { stream ->
                return createBitmapIcon(ImageIO.read(stream), maxWidth, maxHeight)
            }
        } catch (e: Exception) {
            // Not just IOExceptions here; for example, we've seen
            // IllegalArgumentException @ ...... < PNGImageReader:1479 < ... ImageIO.read
            Log.debug("Could not read icon image ${file.presentableUrl}", e)
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
                    val g: Graphics = bg.graphics
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



private val MAX_WIDTH = JBUI.scale(16)
private val MAX_HEIGHT = JBUI.scale(16)

