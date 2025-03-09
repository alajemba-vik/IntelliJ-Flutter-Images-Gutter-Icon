package com.alaje.intellijplugins.flutter_images_gutter_icon

import com.alaje.intellijplugins.flutter_images_gutter_icon.data.GutterIconCache
import com.alaje.intellijplugins.flutter_images_gutter_icon.utils.ImageUtils.hasImageFileExtension
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.util.elementType
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.psi.impl.DartLongTemplateEntryImpl
import com.jetbrains.lang.dart.psi.impl.DartShortTemplateEntryImpl
import com.jetbrains.lang.dart.psi.impl.DartVarDeclarationListImpl
import org.jetbrains.kotlin.psi.psiUtil.PsiChildRange
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.util.prefixIfNot
import java.io.File
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.firstLeaf
import com.jetbrains.lang.dart.DartTokenTypes.*
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.utils.doNothing

class ImageResourceLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun getName(): String = "Flutter image resource gutter preview"

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        //TODO - Allow users to optimize plugin so that it processes elements
        // that are a certain way, for example, only elements that are part of a certain class or file or are string literals
        // that are paths to images. Many of the operations here are expensive and can be avoided if we can filter out

        if (element is DartStringLiteralExpression) {

            if (!element.text.trim('\'', '"').hasImageFileExtension()) return

            val imagePath = getFullImagePath(element)

            if (imagePath.isNotBlank()) {
                addGutterIcon(
                    element,
                    imagePath,
                    result
                )
            }
        }

        if (element is DartReferenceExpression) {
            val elementText = element.text

            // Skip processing if the reference expression is part of a larger expression context.
            // This ensures that nested references, such as "AppDrawables.unifiedSearchEmptyState",
            // are not processed multiple times. For instance, "AppDrawables" and "unifiedSearchEmptyState"
            // would be visited and processed independently if not for this check.
            // So in the case of the example, we'll only process "unifiedSearchEmptyState" and not "AppDrawables.unifiedSearchEmptyState".
            if (elementText.contains(".")) return

            val resolvedReference = element.reference?.resolve()?.context

            if (resolvedReference is DartVarAccessDeclaration) {
                val imagePath = getFullImagePathFromVarDeclaration(resolvedReference)

                if (!imagePath.isNullOrBlank() && imagePath.hasImageFileExtension()) {
                    addGutterIcon(
                        element,
                        imagePath,
                        result
                    )
                }
            }
        }
    }

    private fun getFullImagePath(
        psiElement: PsiElement
    ): String {
        var expressionText = ""

        psiElement.allChildren.forEach { childElement ->

            ProgressManager.checkCanceled()

            when (childElement.elementType) {
                SHORT_TEMPLATE_ENTRY, LONG_TEMPLATE_ENTRY -> {

                    val text = getFullImagePathFromTemplateEntry(childElement as DartPsiCompositeElement)

                    if (!text.isNullOrBlank()) {
                        expressionText += text
                    }
                }

                REGULAR_STRING_PART -> expressionText += childElement.text

                else -> doNothing()
            }
        }

        if (expressionText.isNotBlank() && expressionText.hasImageFileExtension()) {

            // Handle when the asset exists in a package
            if (expressionText.startsWith("packages", ignoreCase = true)) {
                expressionText = expressionText.replaceFirst(
                    Regex("packages${File.separator}[^${File.separator}]+"),
                    ""
                )
            }

            val currentPackagePath = psiElement.containingFile.virtualFile.path.substringBefore("/lib/")
            /// Eg: Users/alajemba/Desktop/HubtelWork/Flutter-Hubtel/app/$_baseUrl/ic_notifications.svg
            expressionText = currentPackagePath + expressionText.prefixIfNot(File.separator)
        }

        return expressionText
    }

    private fun addGutterIcon(
        element: PsiElement,
        imagePath: String,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val project = element.project
        val resourceFile = LocalFileSystem.getInstance().findFileByPath(imagePath) ?: return

        val gutterIconCache = GutterIconCache.getInstance(project)
        val icon = gutterIconCache.getIcon(resourceFile) ?: return

        val relatedItemLineMarkerInfo = NavigationGutterIconBuilder.create(icon)
            .setTarget(resourceFile.toPsiFile(project))
            .setTooltipText("Navigate to image: $imagePath")
            .createLineMarkerInfo(if (element is LeafPsiElement) element else element.firstLeaf())

        result.add(relatedItemLineMarkerInfo)
    }

    private fun getFullImagePathFromVarDeclaration(resolvedReference: DartVarAccessDeclaration): String? {

        val varDeclarationList = resolvedReference.context as? DartVarDeclarationListImpl

        return getFullImagePath(varDeclarationList?.varInit?.expression ?: return null)
    }

    private fun getFullImagePathFromTemplateEntry(templateEntry: DartPsiCompositeElement): String? {
        val expression: DartExpression? = when (templateEntry) {
            is DartShortTemplateEntryImpl -> templateEntry.expression

            is DartLongTemplateEntryImpl -> templateEntry.expression

            else -> return null
        }

        if (expression == null) return null

        var expressionText = expression.text

        when (val resolvedReference = expression.reference?.resolve()?.context) {
            is DartVarAccessDeclaration -> {
                return getFullImagePathFromVarDeclaration(resolvedReference)
            }

            null -> {

                if (expressionText.contains(".")) {
                    expressionText = expressionText.substringAfter(".")
                }

                val variableExpression: DartExpression? =
                    PsiTreeUtil.findChildrenOfType(expression.containingFile, DartVarDeclarationList::class.java)
                    .find { it.varAccessDeclaration.text.contains(expressionText ?: "") }
                    ?.varInit?.expression

                return getFullImagePath(
                    variableExpression ?: return null,
                )
            }
        }

        return null
    }
}