package com.picimako.obsidian.translation;

import static com.intellij.openapi.application.ReadAction.compute;
import static com.picimako.obsidian.translation.TranslationFileUtil.getPropertyPath;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiManager;

import java.util.List;

/**
 * Reads and caches the original, English translation values in {@code en.json}.
 */
public final class TranslationReader {
    private static final Logger LOG = Logger.getInstance(TranslationReader.class);

    /**
     * The reading logic presumes, and thus doesn't perform validation against the following:
     * <ul>
     *     <li>{@code en.json} is present,</li>
     *     <li>the top level value in {@code en.json} is an object</li>
     * </ul>
     */
    public static void readOriginal(Project project) {
        var projectRootDir = ProjectUtil.guessProjectDir(project);
        if (projectRootDir == null) {
            LOG.warn("Could not read translation values from 'en.json' because the project root directory was not found.");
            return;
        }

        var originalLocFile = compute(() -> {
            var enJson = projectRootDir.findFileByRelativePath(ORIGINAL_LOCALIZATION_FILE);
            return (JsonFile) PsiManager.getInstance(project).findFile(enJson);
        });

        readOriginal(originalLocFile, project);
    }

    /**
     * In essence, this is the same as {@link #readOriginal(Project)}, but for cases when the file is already available.
     */
    public static void readOriginal(JsonFile translationFile, Project project) {
        var topLevelObject = (JsonObject) compute(translationFile::getTopLevelValue);
        handlePropertyListCaching(topLevelObject.getPropertyList(), topLevelObject, project);
    }

    private static void handlePropertyListCaching(List<JsonProperty> properties, JsonObject topLevelObject, Project project) {
        for (var property : properties) {
            if (property.getValue() instanceof JsonStringLiteral literal) {
                //Get the path of the property, each property name delimited with a .
                var propertyPathAsList = getPropertyPath(literal, topLevelObject);
                var propertyPath = String.join(".", propertyPathAsList);

                //Save the property literal value mapped to its path
                OriginalLocalizationValuesCache.getInstance(project).addLocalizationValue(propertyPath, literal.getValue());
            } else if (property.getValue() instanceof JsonObject object) {
                handlePropertyListCaching(object.getPropertyList(), topLevelObject, project);
            }
        }
    }
}
