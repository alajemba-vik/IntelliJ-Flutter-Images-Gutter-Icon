package com.alaje.learn.hb_flutter_image_gutter_viewer.settings;

import com.alaje.learn.hb_flutter_image_gutter_viewer.HBImageResourceExternalAnnotator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.client.currentSession
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages

import javax.swing.*;

class ProjectSettingsConfigurable(private val project:Project): Configurable {
    private var settingsView: ProjectSettingsView? = null
    private val persistentState: ProjectSettings.ProjectState
        get() = ProjectSettings.getInstance(project).state

    override fun createComponent(): JComponent? {
        settingsView = ProjectSettingsView()
        return settingsView?.mainPanel;
    }

    override fun isModified(): Boolean {
        return settingsView?.imagesFilePatternTextValue != persistentState.imagesFilePattern
    }

    override fun apply() {
        persistentState.imagesFilePattern = settingsView?.imagesFilePatternTextValue?.also{
            if (it != persistentState.imagesFilePattern) {
                HBImageResourceExternalAnnotator.refreshProject(project)
            }

        }
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsView?.mainPanel
    }


    override fun getDisplayName(): String = "Flutter Images Gutter Icon"

    override fun reset() {
        settingsView?.setImagesFilePatternTextValue(persistentState.imagesFilePattern ?: "")
    }

    override fun disposeUIResources() {
        settingsView = null
    }

}
