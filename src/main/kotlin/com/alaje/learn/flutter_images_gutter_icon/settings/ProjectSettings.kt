package com.alaje.learn.flutter_images_gutter_icon.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project


@Service(Service.Level.PROJECT)
@State(
    name = "alaje.learn.flutter_images_gutter_icon.settings.ProjectSettings",
    reloadable = false,
    storages = [
        Storage(
            "flutter_images_gutter_icon.xml",
            roamingType = RoamingType.PER_OS
            )
    ]
)
class ProjectSettings: PersistentStateComponent<ProjectSettings.ProjectState> {
    private var projectState: ProjectState = ProjectState()


    override fun getState(): ProjectState = projectState

    override fun loadState(ps: ProjectState) {
        projectState = ps
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ProjectSettings {
            return project.service<ProjectSettings>();
        }
    }

    class ProjectState: BaseState() {
        /// This can be a regular string or a regex pattern
        var imagesFilePattern by string()
    }
}