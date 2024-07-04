package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.utils.showWarningNotification
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.IconUtil
import java.io.File
import javax.swing.Icon
import javax.swing.ImageIcon


class HBFlutterGutterImageIconRenderer(val path: String, val project: Project): GutterIconRenderer() {
    private val icon  = IconLoader.getIcon("/icons/jar-gray.png", javaClass)

    override fun equals(other: Any?): Boolean {
        return other is HBFlutterGutterImageIconRenderer
    }

    override fun hashCode(): Int {
        return getIcon().hashCode()
    }


    override fun getIcon(): Icon {
        return ImageIcon(path)
    }

    override fun getTooltipText(): String {
        return "This is a GutterMark!"
    }

}

/*
internal class SimpleLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(element: PsiElement,
        @NotNull result: MutableCollection<in RelatedItemLineMarkerInfo<*>?>
    ) {
        // This must be an element with a literal expression as a parent
        if (element !is PsiJavaTokenImpl || element.parent !is PsiLiteralExpression) {
            return
        }

        // The literal expression must start with the Simple language literal expression
        val value = if (literalExpression.getValue() is String) literalExpression.getValue() else null
        if ((value == null) ||
            !value.startsWith(SimpleAnnotator.SIMPLE_PREFIX_STR + SimpleAnnotator.SIMPLE_SEPARATOR_STR)
        ) {
            return
        }

        // Get the Simple language property usage
        val project: Project = element.project
        val possibleProperties: String = value.substring(
            SimpleAnnotator.SIMPLE_PREFIX_STR.length() + SimpleAnnotator.SIMPLE_SEPARATOR_STR.length()
        )
        val properties: List<SimpleProperty> = SimpleUtil.findProperties(project, possibleProperties)
        if (!properties.isEmpty()) {
            // Add the property to a collection of line marker info
            val builder =
                NavigationGutterIconBuilder.create(SimpleIcons.FILE)
                    .setTargets(properties)
                    .setTooltipText("Navigate to Simple language property")
            result.add(builder.createLineMarkerInfo(element))
        }
    }
}*/