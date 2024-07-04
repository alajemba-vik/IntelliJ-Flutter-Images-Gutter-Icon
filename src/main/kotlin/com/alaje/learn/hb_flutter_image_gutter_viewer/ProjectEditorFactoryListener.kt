package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.HBGutterImageIconManager.addGutterMark
import com.intellij.ide.startup.importSettings.db.KnownPlugins.Dart
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.util.CommentSaver.Companion.tokenType
import org.jetbrains.uast.toUElement


class MyEditorFactoryListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {

        val editor = event.editor
        val project = editor.project
        if (project != null) {

            val psiFile = PsiDocumentManager.getInstance(project)
                .getPsiFile(editor.document)

            val virtualFile = psiFile?.virtualFile

            if (virtualFile != null /*&& isFlutterFile(virtualFile)*/) {
                // class name ends with drawables.dart
                val drawablesClassNameRegex = ".*drawables.dart".toRegex()
                if (!drawablesClassNameRegex.matches(virtualFile.name)) {
                    //project.showWarningNotification("This is not a drawable file${virtualFile.name}")
                    return
                }

                Dart

                psiFile.accept(object : PsiElementVisitor() {

                    override fun visitElement(element: PsiElement) {
                        element.toUElement()?.let {
                            Messages.showInfoMessage("Element: ${it.lang}", "Element")
                        }
                        Messages.showInfoMessage("Element: ${element}", "Element")
                        val containingClass = PsiTreeUtil.getParentOfType(
                            element,
                            PsiClass::class.java
                        )
                        val classElement = element.children.firstOrNull { it.tokenType.toString() == "CLASS_DEFINITION" }
                        Messages.showInfoMessage("found element${classElement}", "Element")
                        element.children.forEach {
                            it.children.forEach {
                                Messages.showInfoMessage("Element: ${it.text}", "Element")
                            }

                        }

                        super.visitElement(element)
                    }
                })
                //psiFile?.accept(DrawableFileVisitor())
                val _packageUrlRegex = """static const \w+ = _packageUrl/\w+\.png";""".toRegex()

               /*
                val pattern = """static const \w+ = "$_baseUrl/\w+\.png";""".toRegex()*/
                /*
                val matcher = pattern.matcher(psiFile.text)
                while (matcher.find()) {
                    val lineNumber = psiFile.viewProvider.document?.getLineNumber(matcher.start()) ?: 0
                    addGutterMark(project, event.editor, lineNumber)
                }*/

                val path = "/demo/assets/drawable/test_image.png"

                val imageFile = project?.basePath?.plus(path)//LocalFileSystem.getInstance().findFileByPath(path)
                //project.showWarningNotification("Image file path: ${virtualFile?.path}")

                Messages.showInfoMessage("Image file path: ${imageFile}", "Image file path")
                addGutterMark(project, event.editor, 0, virtualFile, imageFile ?: "")
            }


        }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        // Clean up if needed
    }
}


/*
class MyProjectManagerListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.showInfoNotification("Flutter image viewer is active")
    }
}*/
