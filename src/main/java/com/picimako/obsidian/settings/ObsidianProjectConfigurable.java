package com.picimako.obsidian.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiManager;
import com.picimako.obsidian.messages.ObsidianBundle;
import com.picimako.obsidian.plugin.ManifestDataCache;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * A Configurable object that acts as the bridge between the Obsidian Settings UI components and the project-level settings.
 */
public final class ObsidianProjectConfigurable implements Configurable {

    private final Project project;
    private ObsidianComponent component;

    public ObsidianProjectConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return ObsidianBundle.message("obsidian.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        component = new ObsidianComponent();
        return component.getSettingsPanel();
    }

    @Override
    public boolean isModified() {
        var settings = ObsidianProjectState.getInstance(project);
        return settings.projectType != component.getSelectedProjectType();
    }

    @Override
    public void apply() {
        var settings = ObsidianProjectState.getInstance(project);
        boolean isModified = isModified();
        settings.projectType = component.getSelectedProjectType();

        //If the settings are modified, update all open files, so that they reflect the changes
        if (isModified) {
            for (var file : FileEditorManager.getInstance(project).getOpenFiles()) {
                var psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(file));
                if (psiFile != null) DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
            }

            //Update the manifest data cache
            if (settings.projectType == ObsidianProjectType.PLUGIN) {
                ManifestDataCache.getInstance(project).cacheProperties();
            }
        }
    }

    @Override
    public void reset() {
        var settings = ObsidianProjectState.getInstance(project);
        component.setProjectType(settings.projectType);
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }
}
