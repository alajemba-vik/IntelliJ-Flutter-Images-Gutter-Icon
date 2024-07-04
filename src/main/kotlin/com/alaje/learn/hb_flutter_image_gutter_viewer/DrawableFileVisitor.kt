package com.alaje.learn.hb_flutter_image_gutter_viewer

import com.intellij.openapi.ui.Messages
import com.intellij.psi.*

class DrawableFileVisitor: JavaElementVisitor() {
    override fun visitClass(aClass: PsiClass) {
        Messages.showInfoMessage("Class: ${aClass.name}", "Class")
        super.visitClass(aClass)
    }
    override fun visitLocalVariable(variable: PsiLocalVariable) {
        Messages.showInfoMessage("Local variable: ${variable.name}", "Local variable")
        super.visitLocalVariable(variable)
    }

    override fun visitReferenceExpression(expression: PsiReferenceExpression) {
        Messages.showInfoMessage("Reference expression: ${expression.text}", "Reference expression")
        super.visitReferenceExpression(expression)
    }

    override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
        Messages.showInfoMessage("Reference element: ${reference.text}", "Reference element")
        super.visitReferenceElement(reference)
    }
}