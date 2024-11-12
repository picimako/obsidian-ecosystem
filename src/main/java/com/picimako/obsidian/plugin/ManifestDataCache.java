package com.picimako.obsidian.plugin;

import static com.intellij.openapi.application.ReadAction.compute;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiManager;

/**
 * Stores data read from the project's {@code manifest.json} file.
 * <p>
 * This cache is updated by {@link ManifestJsonListener}.
 */
@Service(Service.Level.PROJECT)
public final class ManifestDataCache {
    private final Manifest manifest = new Manifest();
    private final Project project;

    public ManifestDataCache(Project project) {
        this.project = project;
    }

    public Manifest getManifest() {
        return manifest;
    }

    /**
     * Reads the {@code manifest.json} and saves relevant property values into the {@link ManifestDataCache}.
     */
    public void cacheProperties() {
        var projectRoot = ProjectUtil.guessProjectDir(project);
        if (projectRoot == null) return;

        var manifestJsonVf = projectRoot.findFileByRelativePath("manifest.json");
        if (manifestJsonVf != null
            && compute(() -> PsiManager.getInstance(project).findFile(manifestJsonVf)) instanceof JsonFile manifestJsonPsi
            && compute(manifestJsonPsi::getTopLevelValue) instanceof JsonObject topLevel) {
            var idProperty = compute(() -> topLevel.findProperty("id"));
            if (idProperty.getValue() instanceof JsonStringLiteral id)
                ManifestDataCache.getInstance(project).getManifest().setId(id.getValue());
        }
    }

    public static ManifestDataCache getInstance(Project project) {
        return project.getService(ManifestDataCache.class);
    }
}
