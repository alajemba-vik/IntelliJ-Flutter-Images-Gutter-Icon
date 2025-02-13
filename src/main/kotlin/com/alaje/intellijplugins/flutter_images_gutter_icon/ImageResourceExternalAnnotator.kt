package com.alaje.intellijplugins.flutter_images_gutter_icon

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.LineMarkerSettings
import com.intellij.ide.EssentialHighlightingMode.isEnabled
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.jetbrains.lang.dart.DartTokenTypes.*
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.psi.impl.DartLongTemplateEntryImpl
import com.jetbrains.lang.dart.psi.impl.DartShortTemplateEntryImpl
import com.jetbrains.lang.dart.psi.impl.DartVarDeclarationListImpl
import org.jetbrains.kotlin.psi.psiUtil.PsiChildRange
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.util.prefixIfNot
import java.io.File


class ImageResourceExternalAnnotator : ExternalAnnotator<
        ImageIconAnnotationInfo?,
        Map<ImageIconAnnotationInfo.AnnotatableElement, GutterIconRenderer>?
        >() {

    private val lineMarkerProvider = LineMarkerProvider()
    private lateinit var project: Project

    // Collects information about the file to be annotated
    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): ImageIconAnnotationInfo? {
        if (!LineMarkerSettings.getSettings().isEnabled(lineMarkerProvider)) {
            return null
        }

        if (isEnabled()) return null

        project = editor.project ?: return null

        val annotationInfo = ImageIconAnnotationInfo(editor)

        val currentPackagePath = file.virtualFile.path.substringBefore("/lib/")

        file.accept(object : DartRecursiveVisitor() {
            override fun visitReferenceExpression(expression: DartReferenceExpression) {

                // Skip processing if the reference expression is part of a larger expression context.
                // This ensures that nested references, such as "AppDrawables.unifiedSearchEmptyState",
                // are not processed multiple times. For instance, "AppDrawables" and "unifiedSearchEmptyState"
                // would be visited and processed independently if not for this check.
                // So in this case, we'll only process the expression if the context is the same as the text.
                if (expression.context?.text != expression.text) return

                val resolvedReference = expression.reference?.resolve()?.context

                if (resolvedReference is DartVarAccessDeclaration) {
                    val varDeclarationText = extractTextFromVarDeclaration(resolvedReference)

                    if (!varDeclarationText.isNullOrBlank()) {

                        if (!varDeclarationText.hasImageFileExtension()) return

                        addAnnotationElementUsingText(
                            text = varDeclarationText,
                            currentPackagePath = currentPackagePath,
                            annotationInfo = annotationInfo,
                            textRange = expression.textRange
                        )
                    }
                }

                super.visitReferenceExpression(expression)
            }

            override fun visitStringLiteralExpression(stringLiteralExpression: DartStringLiteralExpression) {
                if (!stringLiteralExpression.text.hasImageFileExtension()) return

                val finalText = extractTextFromExpression(stringLiteralExpression.allChildren)

                addAnnotationElementUsingText(
                    text = finalText,
                    currentPackagePath = currentPackagePath,
                    annotationInfo = annotationInfo,
                    textRange = stringLiteralExpression.textRange
                )
                super.visitStringLiteralExpression(stringLiteralExpression)
            }
        })

        if (annotationInfo.elements.isEmpty()) {
            return null
        }

        return annotationInfo
    }

    // Collected file information is passed in to collect highlighting data
    override fun doAnnotate(collectedInfo: ImageIconAnnotationInfo?): Map<ImageIconAnnotationInfo.AnnotatableElement, GutterIconRenderer>? {

        val editor = collectedInfo?.editor ?: return null
        val timestamp = collectedInfo.timestamp
        val document = editor.document

        val rendererMap: MutableMap<ImageIconAnnotationInfo.AnnotatableElement, GutterIconRenderer> = HashMap()

        for (element in (collectedInfo.elements)) {
            try {
                ProgressManager.checkCanceled()
            } catch (e: ProcessCanceledException) {
                return null
            }

            if (editor.isDisposed || (document.modificationStamp) > timestamp) {
                return null
            }

            val gutterIconRenderer: GutterIconRenderer? = createGutterIconRenderer(
                project,
                element.image
            )
            if (gutterIconRenderer != null) {
                rendererMap[element] = gutterIconRenderer
            }
        }
        return rendererMap
    }

    // Applies the highlighted data to file
    override fun apply(
        file: PsiFile,
        annotationResult: Map<ImageIconAnnotationInfo.AnnotatableElement, GutterIconRenderer>?,
        holder: AnnotationHolder
    ) {
        annotationResult?.forEach { (annotatableElement, gutterIconRenderer) ->
            // Show image in gutter icon
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(annotatableElement.textRange)
                .gutterIconRenderer(gutterIconRenderer)
                .create()
        }
    }

    // Determines if compatible in Dumb mode (during indexing)
    override fun isDumbAware(): Boolean = true

    private fun extractTextFromExpression(allChildren: PsiChildRange): String {
        var expressionText = ""

        allChildren.forEach { psiElement ->
            when (psiElement.elementType) {
                SHORT_TEMPLATE_ENTRY, LONG_TEMPLATE_ENTRY -> {
                    val text = extractTextFromTemplateEntry(psiElement as DartPsiCompositeElement)
                    if (!text.isNullOrBlank()) {
                        expressionText += text
                    }
                }

                REGULAR_STRING_PART -> expressionText += psiElement.text

                else -> expressionText += ""
            }
        }
        return expressionText
    }

    private fun addAnnotationElementUsingText(
        text: String,
        currentPackagePath: String,
        annotationInfo: ImageIconAnnotationInfo,
        textRange: TextRange
    ) {
        var stringLiteral = text

        if (stringLiteral.isNotBlank()) {

            // Handle when the asset exists in a package
            if (stringLiteral.startsWith("packages", ignoreCase = true)) {
                stringLiteral = stringLiteral.replaceFirst(
                    Regex("packages${File.separator}[^${File.separator}]+"),
                    ""
                )
            }

            /// Eg: Users/alajemba/Desktop/HubtelWork/Flutter-Hubtel/app/$_baseUrl/ic_notifications.svg
            val fullImagePath = currentPackagePath + stringLiteral.prefixIfNot(File.separator)

            annotationInfo.elements.add(
                ImageIconAnnotationInfo.AnnotatableElement(
                    fullImagePath,
                    textRange
                )
            )
        }
    }

    private fun extractTextFromTemplateEntry(templateEntry: DartPsiCompositeElement): String? {
        val expression: DartExpression? = when (templateEntry) {
            is DartShortTemplateEntryImpl -> templateEntry.expression

            is DartLongTemplateEntryImpl -> templateEntry.expression

            else -> return null
        }

        if (expression == null) return null

        val expressionText = expression.text

        when (val resolvedReference = expression.reference?.resolve()?.context) {
            is DartVarAccessDeclaration -> {
                return extractTextFromVarDeclaration(resolvedReference)
            }

            null -> {
                var variableExpression: DartExpression? = null

                expression.containingFile.accept(
                    object : DartRecursiveVisitor() {
                        override fun visitVarDeclarationList(declarationList: DartVarDeclarationList) {
                            if (declarationList.varAccessDeclaration.text.contains(expressionText ?: "")) {
                                variableExpression = declarationList.varInit?.expression
                            }

                            super.visitVarDeclarationList(declarationList)
                        }
                    }
                )

                return extractTextFromExpression(variableExpression?.allChildren ?: return null)
            }
        }

        return null
    }

    private fun extractTextFromVarDeclaration(resolvedReference: DartVarAccessDeclaration): String? {
        val varDeclarationList = resolvedReference.context as? DartVarDeclarationListImpl

        if (varDeclarationList != null) {
            val variableExpression: DartExpression? = varDeclarationList.varInit?.expression
            return extractTextFromExpression(variableExpression?.allChildren ?: return null)
        }

        return null
    }

    private fun createGutterIconRenderer(
        project: Project,
        imagePath: String,
    ): GutterIconRenderer? {
        val resourceFile = LocalFileSystem.getInstance().findFileByPath(imagePath) ?: return null

        return GutterIconRenderer(resourceFile, project)
    }

}

/**
 * Provider used to enable/disable the gutter icons.
 *
 * Read more at https://github.com/JetBrains/android/blob/master/android/src/org/jetbrains/android/AndroidResourceExternalAnnotatorBase.java#L55`
 */
private class LineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): String = "Flutter image resource preview"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null
}

// To force re-highlighting after changing plugin specific settings
fun refreshAnnotators(project: Project) {
    DaemonCodeAnalyzer.getInstance(project).restart()
}

private fun String.hasImageFileExtension(): Boolean {
    return contains(Regex(".*\\.(png|jpg|jpeg|gif|bmp|wbmp|webp|svg)"))
}

