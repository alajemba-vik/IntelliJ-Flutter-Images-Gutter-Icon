package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.HBGutterImageIconManager.addGutterMark
import com.alaje.learn.hb_flutter_image_gutter_viewer.utils.isFlutterFile
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartClassBody
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartFile


class MyEditorFactoryListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {

        val editor = event.editor
        val project = editor.project
        if (project != null) {

            val psiFile = PsiDocumentManager.getInstance(project)
                .getPsiFile(editor.document)

            val virtualFile = psiFile?.virtualFile

            if (virtualFile != null && isFlutterFile(virtualFile)) {
                // class name ends with drawables.dart
                val drawablesClassNameRegex = ".*drawables.dart".toRegex()
                if (!drawablesClassNameRegex.matches(virtualFile.name)) {
                    return
                }


                /*psiFile.accept(object : PsiElementVisitor() {

                    override fun visitElement(element: PsiElement) {

                        if (element is DartFile) {
                            val classFile: DartClass? = element.children.firstOrNull {
                                it is DartClass && it.name?.contains("Drawables") == true
                            } as? DartClass


                            val iconProjService = IconProjectService.getInstance(project)

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
                                }

                                if (!isBaseUrl && !isPackageUrl) {
                                    imageUrl = packageUrl + baseUrl + (variableExpression?.getAssignedString ?: "")

                                    if (imageUrl.isNotBlank()) {
                                        val imageFile = project.basePath?.plus(imageUrl)


                                        val lineNum = psiFile.viewProvider.document?.getLineNumber(
                                            variable.textRange.startOffset
                                        ) ?: 0

                                        addGutterMark(
                                            event.editor,
                                            lineNum,
                                            virtualFile,
                                            imageFile ?: ""
                                        )
                                    }
                                }

                            }

                            iconProjService.loadImage(classFile, psiFile, virtualFile, event)


                        }
                        super.visitElement(element)
                    }
                })*/
            }


        }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        // Clean up if needed
    }

}


