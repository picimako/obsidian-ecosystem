package com.picimako.obsidian.settings;

/**
 * The type of Obsidian project.
 */
public enum ObsidianProjectType {
    NONE("None"), PLUGIN("Plugin"), THEME("Theme");

    private final String projectType;

    ObsidianProjectType(String projectType) {
        this.projectType = projectType;
    }

    @Override
    public String toString() {
        return projectType;
    }
}
