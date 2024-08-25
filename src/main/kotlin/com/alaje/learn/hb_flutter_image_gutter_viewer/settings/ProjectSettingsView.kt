package com.alaje.learn.hb_flutter_image_gutter_viewer.settings

import com.alaje.learn.hb_flutter_image_gutter_viewer.utils.Constants.Companion.REMOTE_SOURCE_CODE_README
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
        _mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                "Images file and class pattern: ",
                imagesFilePatternTextField,
                1,
                false
            )
            .addComponent(imagesFilePatternInfo())
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun setImagesFilePatternTextValue(newText: String) {
        imagesFilePatternTextField.text = newText
    }
}

private fun imagesFilePatternInfo(): DialogPanel {
    return panel {
        rowsRange {
            row {
                comment(
                    """
                    Supported patterns include regular strings and regex patterns e.g., "images" or ".*_images".
                    You can separate several patterns using a pipe e.g., "images|assets|.*_images".
                    Exclude the quotes.
                    """
                )
            }
            row {
                comment(
                    """
                    The value of this field is used to determine the name of the dart file and class 
                    that contains references to the image resources. E.g., if the value is "images",
                    the plugin will acknowledge any dart file that contains "images" and within that file any
                    class whose name contains "images".
                    """
                )
            }
            row {
                comment("<icon src='AllIcons.General.Information'>&nbsp;See the <a href='${REMOTE_SOURCE_CODE_README}'>README</a> on Github for all information")
            }
        }
    }
}