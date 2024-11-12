package com.picimako.obsidian.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.picimako.obsidian.messages.ObsidianBundle;

import javax.swing.*;

/**
 * Provides the UI components for the plugin settings page.
 */
public final class ObsidianComponent {
    private final JPanel settingsPanel;
    private final ComboBox<ObsidianProjectType> obsidianProjectType;

    public ObsidianComponent() {
        obsidianProjectType = new ComboBox<>(ObsidianProjectType.values());

        settingsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel(ObsidianBundle.message("settings.obsidian.project.type")), obsidianProjectType)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    //Setters, getters

    public ObsidianProjectType getSelectedProjectType() {
        return obsidianProjectType.getItem();
    }

    public void setProjectType(ObsidianProjectType projectType) {
        obsidianProjectType.setItem(projectType);
    }
}
