package com.alaje.learn.hb_flutter_image_gutter_viewer.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;

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
        persistentState.imagesFilePattern = settingsView?.imagesFilePatternTextValue
    }

    override fun getDisplayName(): String {
        TODO("Not yet implemented")
    }

    override fun reset() {
        settingsView?.setImagesFilePatternTextValue(persistentState.imagesFilePattern ?: "")
    }

    override fun disposeUIResources() {
        settingsView = null
    }

}
