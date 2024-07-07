
package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.alaje.learn.hb_flutter_image_gutter_viewer.HBGutterImageIconManager.addGutterMark
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartExpression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.DartClassBody
import kotlinx.coroutines.Dispatchers

@Service(Service.Level.PROJECT)
class IconProjectService(
    private val project: Project,
    private val cs: CoroutineScope
) {
    /*fun loadImage(classFile: DartClass?, psiFile: PsiFile, virtualFile: VirtualFile, event: EditorFactoryEvent) {
        *//*
                           * static const _packageUrl = "packages/demo";
                           * static const _baseUrl = "$_packageUrl/assets/drawable";
                           * *//*
        val classBody: DartClassBody? = classFile?.children?.firstOrNull {
            it is DartClassBody
        } as? DartClassBody

        var packageUrl = "";
        var baseUrl = ""

        cs.launch(Dispatchers.Default) {
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
        }
    }

    companion object {
        @JvmStatic fun getInstance(project: Project): IconProjectService = project.service()
    }*/
}

/*
class MyProjectManagerListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.showInfoNotification("Flutter image viewer is active")
    }
}*/

val PsiElement.getAssignedString: String get() {
    return childLeafs().firstOrNull{it.isStringAssigned}?.text ?: ""
}


private val PsiElement.isStringAssigned: Boolean get() {
    return elementType == DartTokenTypes.REGULAR_STRING_PART
}
