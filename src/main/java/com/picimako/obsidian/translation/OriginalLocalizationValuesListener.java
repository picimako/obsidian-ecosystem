package com.picimako.obsidian.translation;

import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;

import com.intellij.json.psi.JsonFile;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Re-caches the values in {@link OriginalLocalizationValuesCache} from the entire {@code en.json} files upon changes in that file.
 */
public class OriginalLocalizationValuesListener extends PsiTreeChangeAdapter {

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
        if (file instanceof JsonFile jsonFile && ORIGINAL_LOCALIZATION_FILE.equals(file.getName())) {
            //NOTE: In case this would cause performance issues, restrict the caching to a smaller chunk
            // of the file around the changes.
            TranslationReader.readOriginal(jsonFile, file.getProject());
        }
    }
}
