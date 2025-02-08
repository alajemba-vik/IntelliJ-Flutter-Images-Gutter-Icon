package com.alaje.intellijplugins.flutter_images_gutter_icon

import com.alaje.intellijplugins.flutter_images_gutter_icon.settings.ProjectSettings
import com.alaje.intellijplugins.flutter_images_gutter_icon.utils.GutterIconCache
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.LineMarkerSettings
import com.intellij.ide.EssentialHighlightingMode.isEnabled
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.util.PathUtil
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.psi.impl.*
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.util.prefixIfNot
import java.io.File
import java.util.regex.PatternSyntaxException


class ImageResourceExternalAnnotator  : ExternalAnnotator<
        ImageIconAnnotationInfo?,
        Map<ImageIconAnnotationInfo.AnnotatableElement, GutterIconRenderer>?
        >(){

    private val lineMarkerProvider = LineMarkerProvider()

    // Collects information about the file to be annotated
    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): ImageIconAnnotationInfo? {
        if (!LineMarkerSettings.getSettings().isEnabled(lineMarkerProvider)) {
            return null
        }

        if (isEnabled()) return null

        val annotationInfo = ImageIconAnnotationInfo(editor)

        val projectSettings = ProjectSettings.getInstance(file.project);

        val imagesFilePattern = (projectSettings.state.imagesFilePattern ?: "").ifBlank { defaultFilePattern }

        val imagesFilePatternsRegex = try {
            Regex("(?i)(${imagesFilePattern})")
        } catch (e: PatternSyntaxException) {
            // TODO: Inform user of invalid regex pattern
            println("Invalid regex pattern: ${e.message}")
            return null
        }

        file.accept(object : PsiElementVisitor() {

            override fun visitElement(element: PsiElement) {

                if (element is DartFile && element.name.contains(imagesFilePatternsRegex)) {
                    val currentPackagePath = element.containingDirectory?.virtualFile?.path?.substringBefore("/lib/") ?: ""

                    for (entity in element.children) {

                        when {
                            entity.elementType == DartTokenTypes.VAR_DECLARATION_LIST -> {
                                addAnnotationElementUsingVariable(
                                    entity as DartVarDeclarationList,
                                    currentPackagePath,
                                    annotationInfo
                                )
                            }

                            entity is DartClass -> {
                                for (classChild in entity.children) {

                                    if (classChild is DartClassBody) {

                                        val varDeclarationList =
                                            classChild.classMembers?.varDeclarationListList ?: emptyList();

                                        for (variable in varDeclarationList) {
                                            addAnnotationElementUsingVariable(
                                                variable,
                                                currentPackagePath,
                                                annotationInfo
                                            )
                                        }
                                    }
                                }

                            }
                        }

                    }
                }
                super.visitElement(element)
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
        val project = editor.project ?: return null
        val timestamp = collectedInfo.timestamp
        val document = editor.document

        val rendererMap: MutableMap<ImageIconAnnotationInfo.AnnotatableElement, GutterIconRenderer> = HashMap()

        for (element in (collectedInfo.elements)) {
            try {
                ProgressManager.checkCanceled()
            } catch (e: ProcessCanceledException) {
                return  null
            }

            if (editor.isDisposed || (document.modificationStamp) > timestamp) {
                return null
            }

            val gutterIconRenderer: GutterIconRenderer? = getResourceGutterIconRenderer(
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
        GutterIconCache.getInstance(file.project).renderIcon(annotationResult, holder)
    }

    // Determines if compatible in Dumb mode (during indexing)
    override fun isDumbAware(): Boolean {
        return true
    }

    private fun addAnnotationElementUsingVariable(
        variable: DartVarDeclarationList,
        currentPackagePath: String,
        annotationInfo: ImageIconAnnotationInfo
    ) {
        val variableExpression: DartExpression? = variable.varInit?.expression
        var variableValue = extractAllExpressionText(variableExpression)

        if (variableValue.isNotBlank()) {

            // Handle when the asset exists in a package
            if (variableValue.startsWith("packages", ignoreCase = true)){
                variableValue = variableValue.replaceFirst(
                    Regex("packages${File.separator}[^${File.separator}]+"),
                    ""
                )
            }

            val fullImagePath = currentPackagePath + variableValue.prefixIfNot(File.separator)

            // To avoid adding non-file paths
            if (PathUtil.getFileExtension(fullImagePath) != null) {

                annotationInfo.elements.add(
                    ImageIconAnnotationInfo.AnnotatableElement(
                        fullImagePath,
                        variable.textRange
                    )
                )
            }
        }
    }

    private fun extractAllExpressionText(
        variableExpression: DartExpression?,
    ): String {
        var variableValue  = ""
        variableExpression?.node?.children()?.forEach { variableElement ->
            when (variableElement.elementType) {
                DartTokenTypes.SHORT_TEMPLATE_ENTRY, DartTokenTypes.LONG_TEMPLATE_ENTRY -> {
                    val templateExpression: DartExpression? =
                        if (variableElement.elementType == DartTokenTypes.SHORT_TEMPLATE_ENTRY) {
                            (variableElement.psi as? DartShortTemplateEntryImpl)?.expression
                        } else {
                            (variableElement.psi as? DartLongTemplateEntryImpl)?.expression
                        }

                    val expressionRef = templateExpression as? DartReferenceExpressionImpl

                    val expression: DartVarDeclarationListImpl? = expressionRef?.let { entry ->
                        val resolvedTarget = entry.resolve()?.context as? DartVarAccessDeclarationImpl
                        resolvedTarget?.context as? DartVarDeclarationListImpl
                    }

                    if (expression != null) {
                        variableValue += extractAllExpressionText(expression.varInit?.expression)
                    }
                }

                DartTokenTypes.REGULAR_STRING_PART -> {
                    variableValue += variableElement.text
                }

                DartTokenTypes.ARGUMENTS -> {
                    variableValue += (variableElement.psi as DartArgumentsImpl).argumentList?.expressionList?.joinToString(
                        ""
                    ) { expression ->
                        if (expression is DartStringLiteralExpressionImpl) {
                            val text = (expression as? DartStringLiteralExpressionImpl)?.text
                            text?.replace(singleAndDoubleQuotesRegex, "") ?: ""
                        } else {
                            extractAllExpressionText(expression)
                        }

                    } ?: ""
                }
            }
        }
        return variableValue
    }

    private fun getResourceGutterIconRenderer(
        project: Project,
        imagePath: String,
    ): GutterIconRenderer? {
        val resourceFile = LocalFileSystem.getInstance().findFileByPath(imagePath) ?: return null
        // Updating the GutterIconCache in the background thread to include the icon.
        GutterIconCache.getInstance(project).getIcon(resourceFile)
        return FlutterGutterImageIconRenderer(resourceFile, project)
    }

    /**
     * Provider used to enable/disable Android resource gutter icons.
     *
     *
     * This provider doesn't directly provide any of the resource gutter icons; that's done by
     * [ImageResourceExternalAnnotator]. But since those are [ExternalAnnotator]s, they don't show up in Gutter icon
     * settings. This provider does show up in settings, and the other annotators check its value to determine if they should be enabled.
     */
    class LineMarkerProvider : LineMarkerProviderDescriptor() {
        override fun getName(): String {
            return "Flutter image resource preview"
        }

        override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
            return null
        }
    }

}

// To force re-highlighting after changing plugin specific settings
fun refreshAnnotators(project: Project) {
    DaemonCodeAnalyzer.getInstance(project).restart()
}


private const val defaultFilePattern = "drawables"
private val singleAndDoubleQuotesRegex = Regex("""^['"]|['"]$""")
