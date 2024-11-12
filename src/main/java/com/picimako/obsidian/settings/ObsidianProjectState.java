package com.picimako.obsidian.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * Stores the project-level settings of this plugin.
 */
@State(
    name = "Obsidian Project Settings",
    storages = @Storage(value = "obsidian_project_settings.xml", exportable = true)
)
public final class ObsidianProjectState implements PersistentStateComponent<ObsidianProjectState> {

    public ObsidianProjectType projectType = ObsidianProjectType.NONE;

    @Override
    public ObsidianProjectState getState() {
        return this;
    }

    @Override
    public void loadState(ObsidianProjectState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static ObsidianProjectState getInstance(Project project) {
        return project.getService(ObsidianProjectState.class);
    }

    public static boolean isPluginProject(Project project) {
        return ObsidianProjectState.getInstance(project).projectType == ObsidianProjectType.PLUGIN;
    }

    public static boolean isThemeProject(Project project) {
        return ObsidianProjectState.getInstance(project).projectType == ObsidianProjectType.THEME;
    }
}
