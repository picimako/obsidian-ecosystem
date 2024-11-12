package com.picimako.obsidian.plugin;

import com.intellij.json.psi.JsonFile;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Re-caches the values from the {@code manifest.json} file upon changes in that file.
 */
public class ManifestJsonListener extends PsiTreeChangeAdapter {

    @Override
    public void childrenChanged(@NotNull final PsiTreeChangeEvent event) {
        handleChange(event);
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
        handleChange(event);
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
        handleChange(event);
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        handleChange(event);
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        handleChange(event);
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
        handleChange(event);
    }

    private void handleChange(@NotNull PsiTreeChangeEvent event) {
        var file = event.getFile();
        if (file instanceof JsonFile jsonFile && "manifest.json".equals(file.getName())) {
            ManifestDataCache.getInstance(jsonFile.getProject()).cacheProperties();
        }
    }
}
