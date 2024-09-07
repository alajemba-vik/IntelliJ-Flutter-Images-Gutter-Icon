package com.alaje.intellijplugins.flutter_images_gutter_icon

import com.alaje.intellijplugins.flutter_images_gutter_icon.settings.ProjectSettings
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.EssentialHighlightingMode.isEnabled
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.util.PathUtil
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.psi.impl.*
import org.jetbrains.kotlin.psi.psiUtil.children


class HBImageResourceExternalAnnotator :  BaseHBImageResourceExternalAnnotator(){

    override fun collectInformation(file: PsiFile, editor: Editor, imagePath: String): FileAnnotationInfo? {
        if (isEnabled()) return null

        val annotationInfo = FileAnnotationInfo(editor)

        val projectSettings = ProjectSettings.getInstance(file.project);

        val imagesFilePattern = (projectSettings.state.imagesFilePattern ?: "").ifBlank { defaultFilePattern }

        val imagesFilePatternsRegex = Regex("(?i)(${imagesFilePattern})")

        file.accept(object : PsiElementVisitor() {

            override fun visitElement(element: PsiElement) {

                if (element is DartFile && element.name.contains(imagesFilePatternsRegex)) {
                    val currentPackagePath = element.containingDirectory?.virtualFile?.path?.substringBefore("/lib/") ?: ""

                    val imageBaseUrl = currentPackagePath.substringBefore("lib/")

                    for (entity in element.children) {

                        when {
                            entity.elementType == DartTokenTypes.VAR_DECLARATION_LIST -> {
                                addAnnotationElementUsingVariable(
                                    entity as DartVarDeclarationList,
                                    imageBaseUrl,
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
                                                imageBaseUrl,
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

    private fun addAnnotationElementUsingVariable(
        variable: DartVarDeclarationList,
        imageBaseUrl: String,
        annotationInfo: FileAnnotationInfo
    ) {
        val variableExpression: DartExpression? = variable.varInit?.expression
        val variableValue = extractAllExpressionText(variableExpression)

        if (variableValue.isNotBlank()) {
            val fullImagePath = imageBaseUrl + variableValue

            // To avoid adding non-file paths
            if (PathUtil.getFileExtension(fullImagePath) != null) {

                thisLogger().debug(fullImagePath)

                annotationInfo.elements.add(
                    FileAnnotationInfo.AnnotatableElement(
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
        variableExpression?.node?.children()?.forEach {
            when (it.elementType) {
                DartTokenTypes.SHORT_TEMPLATE_ENTRY, DartTokenTypes.LONG_TEMPLATE_ENTRY -> {
                    val templateExpression: DartExpression? =
                        if (it.elementType == DartTokenTypes.SHORT_TEMPLATE_ENTRY) {
                            (it.psi as? DartShortTemplateEntryImpl)?.expression
                        } else {
                            (it.psi as? DartLongTemplateEntryImpl)?.expression
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
                    variableValue += it.text
                }
            }
        }
        return variableValue
    }

}

private const val defaultFilePattern = "drawables"

fun refreshAnnotators(project: Project) {
    DaemonCodeAnalyzer.getInstance(project).restart()
}