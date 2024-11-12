package com.picimako.obsidian.translation;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * Project service for storing the original, English localization values each mapped to its property path
 * inside the translation files.
 * <p>
 * This is put into place, so that the English JSON file's PSI don't have to be re-read and traversed every time
 * a value is needed from it.
 */
@Service(Service.Level.PROJECT)
public final class OriginalLocalizationValuesCache {

    /**
     * JSON property path -> original English localization value.
     */
    private final Map<String, String> originalValues = new HashMap<>();
    private boolean isProjectObsidianTranslations;

    @SuppressWarnings("unused")
    public OriginalLocalizationValuesCache(Project project) {
    }

    public Map<String, String> getOriginalValues() {
        return originalValues;
    }

    /**
     * Adds a localization value for the provided JSON property {@code path}.
     */
    public void addLocalizationValue(String path, String value) {
        originalValues.put(path, value);
    }

    public boolean isProjectObsidianTranslations() {
        return isProjectObsidianTranslations;
    }

    public void setProjectObsidianTranslations(boolean projectObsidianTranslations) {
        isProjectObsidianTranslations = projectObsidianTranslations;
    }

    public static OriginalLocalizationValuesCache getInstance(Project project) {
        return project.getService(OriginalLocalizationValuesCache.class);
    }

    public static boolean isProjectObsidianTranslations(Project project) {
        return OriginalLocalizationValuesCache.getInstance(project).isProjectObsidianTranslations();
    }
}
