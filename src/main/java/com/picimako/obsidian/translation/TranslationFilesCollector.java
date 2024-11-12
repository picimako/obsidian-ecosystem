package com.picimako.obsidian.translation;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Provides utilities for collecting translation files.
 */
public final class TranslationFilesCollector {
    public static final String ORIGINAL_LOCALIZATION_FILE = "en.json";

    /**
     * Collects all translation files from the project except from {@code fileAtInvocation}.
     *
     * @param projectRootDir   the project root directory
     * @param project          the current project
     * @param fileAtInvocation the file in which an intention action is invoked
     */
    public static List<PsiFile> collectTranslationFiles(VirtualFile projectRootDir, Project project, PsiFile fileAtInvocation) {
        return collectTranslationFiles(projectRootDir, project, fileAtInvocation, false);
    }

    /**
     * Collects all translation files from the project, including/excluding {@code fileAtInvocation} based
     * on the {@code includeFileAtInvocation} flag.
     *
     * @param projectRootDir          the project root directory
     * @param project                 the current project
     * @param fileAtInvocation        the file in which an intention action is invoked
     * @param includeFileAtInvocation if {@code fileAtInvocation} is included in the returned list of files
     */
    public static List<PsiFile> collectTranslationFiles(VirtualFile projectRootDir, Project project, PsiFile fileAtInvocation, boolean includeFileAtInvocation) {
        return Arrays.stream(projectRootDir.getChildren())
            .filter(child -> !child.isDirectory()) //Excludes directories
            .filter(file -> "json".equals(file.getExtension())) //Excludes non-JSON files
            //Excludes the package.json, .eslintrc.json, the file where the intention is invoked
            .filter(jsonFile -> {
                String fileName = jsonFile.getName();
                boolean isNotPackageOrEslintrcJson = !TranslationFileUtil.isPackageOrEslintrcJson(fileName);
                return includeFileAtInvocation
                       ? isNotPackageOrEslintrcJson
                       : isNotPackageOrEslintrcJson && !fileName.equals(fileAtInvocation.getName());
            }).map(jsonFile -> ReadAction.compute(() -> PsiManager.getInstance(project).findFile(jsonFile)))
            .filter(Objects::nonNull)
            .toList();
    }

    private TranslationFilesCollector() {
        //Utility class
    }
}
