package com.alaje.learn.hb_flutter_image_gutter_viewer.utils

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiTreeUtil

fun isFlutterFile(containingFile: VirtualFile?): Boolean {
    return containingFile?.name?.endsWith(".dart") ?: false
}
/*
* final class DemoDrawables {
  DemoDrawables._();

  static const _packageUrl = "packages/demo";
  static const _baseUrl = "$_packageUrl/assets/drawable";

  static const testImage = "$_baseUrl/test_image.png";
}

* */
/*
fun containsDrawables(containingFile: VirtualFile?): Boolean {
    //check that it contains a class with the name DemoDrawables
    //check that it contains a static const field with the name testImage

    */
/*containingFile.findChild()
    val demoDrawablesClass = PsiTreeUtil.findChildrenOfType(psiFile, KtClass::class.java)
        .firstOrNull { it.name == "DemoDrawables" } ?: return false
*//*


}*/
