package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.settings.ProjectSettings
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.EssentialHighlightingMode.isEnabled
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartClassBody
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartFile


class HBImageResourceExternalAnnotator :  BaseHBImageResourceExternalAnnotator(){

    override fun collectInformation(file: PsiFile, editor: Editor, imagePath: String): FileAnnotationInfo? {
        if (isEnabled()) return null
        val annotationInfo = FileAnnotationInfo(file, editor)

        val projectSettings = ProjectSettings.getInstance(file.project);

        val imagesFilePattern = projectSettings.state.imagesFilePattern ?: defaultFilePattern;
        val imagesFilePatternRegex = Regex("(?i)$imagesFilePattern")

        file.accept(object : PsiElementVisitor() {

            override fun visitElement(element: PsiElement) {

                if (element is DartFile && element.name.contains(imagesFilePatternRegex)) {

                    val classFile: DartClass? = element.children.firstOrNull {
                        it is DartClass && it.name?.contains(imagesFilePatternRegex) == true
                    } as? DartClass

                    val classBody: DartClassBody? = classFile?.children?.firstOrNull {
                        it is DartClassBody
                    } as? DartClassBody

                    var packageUrl = "";
                    var baseUrl = ""

                    for (variable in (classBody?.classMembers?.varDeclarationListList ?: emptyList())) {
                        val nameOfVariable = variable.varAccessDeclaration.name
                        val variableExpression: DartExpression? = variable.varInit?.expression

                        val isPackageUrl = nameOfVariable == "_packageUrl"
                        val isBaseUrl = nameOfVariable == "_baseUrl"

                        var imageUrl: String

                        if (isPackageUrl) {
                            packageUrl = variableExpression?.getAssignedString ?: ""
                            packageUrl = packageUrl.replace("packages", "")
                        }

                        if (isBaseUrl) {
                            baseUrl = variableExpression?.getAssignedString ?: ""

                            //Users/username/Development/Flutter-Hubtel/app/lib/ux/resources/app_drawables.dart
                            //Users/username/Development/Flutter-Hubtel/base_feature_quick_commerce/lib/ux/resources/drawables.dart
                            //Users/username/Development/Flutter-Hubtel/base_feature_quick_commerce/assets/drawable/animated_ongoing_order.gif
                            // println("${editor.virtualFile?.path}");*/
                            if (packageUrl.isBlank()) {
                                packageUrl = "/app/";
                            }
                        }

                        if (!isBaseUrl && !isPackageUrl) {
                            imageUrl = packageUrl + baseUrl + (variableExpression?.getAssignedString ?: "")

                            if (imageUrl.isNotBlank()) {

                                imageUrl = editor.project?.basePath?.plus(imageUrl) ?: ""

                                annotationInfo.elements.add(
                                    FileAnnotationInfo.AnnotatableElement(
                                        imageUrl,
                                        variable.textRange
                                    )
                                )
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

    companion object{
        fun refreshProject(project: Project) {
            DaemonCodeAnalyzer.getInstance(project).restart()
            /*val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
            val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return

            val annotationInfo = collectInformation(psiFile, editor, "")
            if (annotationInfo != null) {
                apply(
                    psiFile,
                    annotationInfo,
                    AnnotationHolder(project, editor)
                )
            }*/
        }
    }
}


val PsiElement.getAssignedString: String get() {
    return childLeafs().firstOrNull{it.isStringAssigned}?.text ?: ""
}


private val PsiElement.isStringAssigned: Boolean get() {
    return elementType == DartTokenTypes.REGULAR_STRING_PART
}


private val defaultFilePattern = "drawables"