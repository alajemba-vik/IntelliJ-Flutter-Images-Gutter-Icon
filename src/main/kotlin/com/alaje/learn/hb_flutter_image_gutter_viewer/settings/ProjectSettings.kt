package com.alaje.learn.hb_flutter_image_gutter_viewer.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project


@Service(Service.Level.PROJECT)
@State(
    name = "alaje.learn.hb_flutter_image_gutter_viewer.settings.ProjectSettings",
    reloadable = false,
    storages = [
        Storage(
            "hb_flutter_image_gutter_viewer.xml",
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
        var imagesFilePattern by string()
    }
}