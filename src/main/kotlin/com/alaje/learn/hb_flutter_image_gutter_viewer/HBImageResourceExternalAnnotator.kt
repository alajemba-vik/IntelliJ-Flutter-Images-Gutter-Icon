package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.intellij.ide.EssentialHighlightingMode.isEnabled
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.platform.diagnostic.telemetry.EDT
import com.intellij.psi.*
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartClassBody
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HBImageResourceExternalAnnotator :  BaseHBImageResourceExternalAnnotator(){

    override fun collectInformation(file: PsiFile, editor: Editor, imagePath: String): FileAnnotationInfo? {
        if (isEnabled()) return null
        val annotationInfo = FileAnnotationInfo(file, editor)
        /*file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitReferenceElement(element: PsiJavaCodeReferenceElement) {
                annotationInfo.elements.add(FileAnnotationInfo.AnnotatableElement(element))
            }
        })
*/
        file.accept(object : PsiElementVisitor() {

            override fun visitElement(element: PsiElement) {


                if (element is DartFile) {
                    val classFile: DartClass? = element.children.firstOrNull {
                        it is DartClass && it.name?.contains("Drawables") == true
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
}


val PsiElement.getAssignedString: String get() {
    return childLeafs().firstOrNull{it.isStringAssigned}?.text ?: ""
}


private val PsiElement.isStringAssigned: Boolean get() {
    return elementType == DartTokenTypes.REGULAR_STRING_PART
}

