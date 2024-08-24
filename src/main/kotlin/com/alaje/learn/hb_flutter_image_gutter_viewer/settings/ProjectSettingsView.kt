package com.alaje.learn.hb_flutter_image_gutter_viewer.settings

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class ProjectSettingsView {
    private var _mainPanel : JPanel
    val mainPanel: JPanel
        get() = _mainPanel

    private val  imagesFilePatternTextField: JBTextField = JBTextField()


    val imagesFilePatternTextValue : String
        get() = imagesFilePatternTextField.text

    init {
        /*imagesFilePatternTextField.emptyText.setText(
            "Enter dart file pattern (e.g., *_images, *_drawables, *_assets, *_icons)"
        )*/
        _mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                "Images file pattern: ",
                imagesFilePatternTextField,
                1,
                true
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun setImagesFilePatternTextValue(newText: String) {
        imagesFilePatternTextField.text = newText
    }
}

private fun imagesFilePatternInfo(): DialogPanel {
    return panel {

    }
}