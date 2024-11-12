package com.picimako.obsidian.translation;

import static com.intellij.openapi.application.ReadAction.compute;
import static com.intellij.psi.util.PsiTreeUtil.collectParents;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Various utilities for JSON translation files.
 */
public final class TranslationFileUtil {
    public static final String PACKAGE_JSON = "package.json";
    public static final String ESLINTRC_JSON = ".eslintrc.json";

    /**
     * Gets the path of the property as list of property names leading to it.
     *
     * @param startElement the element to get the path of
     * @param endElement   used for the stop condition as the last element to stop the parent property collection at
     */
    public static List<String> getPropertyPath(PsiElement startElement, PsiElement endElement) {
        var propertyPath = compute(() -> collectParents(startElement, JsonProperty.class, true, e -> e.isEquivalentTo(endElement)))
            .stream()
            .map(JsonProperty::getName)
            .collect(Collectors.toList());
        Collections.reverse(propertyPath);

        return propertyPath;
    }

    /**
     * Returns the JSON property from the containing file of {@code topLevelObject} at the given {@code path}.
     *
     * @param path           contains the property names, in order, that point to the property to find
     * @param topLevelObject the top level object in a translation file
     */
    public static JsonProperty findPropertyByPath(List<String> path, JsonObject topLevelObject) {
        var property = compute(() -> topLevelObject.findProperty(path.get(0)));

        for (int i = 1; i < path.size(); i++) {
            final int index = i;
            //Go one level deeper in the property hierarchy
            if (property.getValue() instanceof JsonObject objectValue) {
                property = compute(() -> objectValue.findProperty(path.get(index)));
                continue;
            }

            //If we are processing the last property name, we reached the property
            if (i == path.size() - 1) return property;
        }

        return property;
    }

    /**
     * Returns the top level value from the provided file as a {@link JsonObject}.
     * <p>
     * This method assumes that the top level value is an object.
     */
    public static JsonObject getTopLevelObjectOf(PsiFile jsonFile) {
        return compute(() -> (JsonObject) ((JsonFile) jsonFile).getTopLevelValue());
    }

    public static boolean isPackageOrEslintrcJson(String fileName) {
        return fileName.equals(PACKAGE_JSON) || fileName.equals(ESLINTRC_JSON);
    }

    private TranslationFileUtil() {
        //Utility class
    }
}
