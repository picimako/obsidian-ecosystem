package com.picimako.obsidian.translation;

import static com.intellij.openapi.application.ReadAction.computeBlocking;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiManager;
import ini4idea.lang.psi.IniFile;
import ini4idea.lang.psi.IniSection;
import org.jetbrains.annotations.NotNull;

/**
 * Reads and caches the original, English translation values in {@code en.txt}.
 */
public final class TranslationReader {
    /**
     * The reading logic presumes, and thus doesn't perform validation against the following:
     * <ul>
     *     <li>{@code en.txt} is present,</li>
     * </ul>
     */
    public static void readOriginal(Project project) {
        var projectRootDir = ProjectUtil.guessProjectDir(project);
        if (projectRootDir == null) {
            Logger.getInstance(TranslationReader.class).warn("Could not read translation values from 'en.txt' because the project root directory was not found.");
            return;
        }

        var originalLocFile = computeBlocking(() -> {
            var translationsDir = projectRootDir.findFileByRelativePath("translations");
            if (translationsDir == null) return null;

            var enTxt = translationsDir.findFileByRelativePath(ORIGINAL_LOCALIZATION_FILE);
            if (enTxt == null) return null;


            var enTxtFile = PsiManager.getInstance(project).findFile(enTxt);
            return enTxtFile instanceof IniFile iniFile ? iniFile : enTxtFile;
        });

        if (!(originalLocFile instanceof IniFile originalLocIniFile)) return;

        readOriginal(originalLocIniFile, project);
    }

    /**
     * In essence, this is the same as {@link #readOriginal(Project)}, but for cases when the file is already available.
     */
    public static void readOriginal(@NotNull IniFile translationFile, Project project) {
        for (var e : computeBlocking(translationFile::getChildren)) {
            if (e instanceof IniSection section) {
                var sectionName = computeBlocking(section::getNameText);
                var originalValue = computeBlocking(() -> section.getIniPropertyList().getFirst().getIniValue());
                if (originalValue == null) continue;

                var original = computeBlocking(originalValue::getText);

                //Save the original value mapped to its section name
                OriginalLocalizationValuesCache.getInstance(project).addLocalizationValue(sectionName, original);
            }
        }
    }
}
