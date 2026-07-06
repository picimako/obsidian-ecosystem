package com.picimako.obsidian.translation;

import static com.intellij.openapi.application.ReadAction.computeBlocking;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import ini4idea.lang.psi.IniFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides utilities for collecting translation files.
 */
public final class TranslationFilesCollector {
    public static final String ORIGINAL_LOCALIZATION_FILE = "en.txt";

    public static boolean isOriginalLocFile(PsiFile file) {
        if (file == null) return false;
        String fileName = file.getName();
        return fileName.startsWith("en") && fileName.endsWith(".txt");
    }

    /**
     * Collects all app translation files from the project
     *
     * @param projectRootDir the project root directory
     * @param project        the current project
     */
    public static List<IniFile> collectAppTranslationFiles(VirtualFile projectRootDir, Project project) {
        var translationsDir = ContainerUtil.filter(projectRootDir.getChildren(), child -> child.isDirectory() && "translations".equals(child.getName()));
        if (translationsDir.size() != 1) return Collections.emptyList();

        return Arrays.stream(translationsDir.getFirst().getChildren())
            .filter(child -> !child.isDirectory()) //Excludes directories
            .filter(file -> "txt".equals(file.getExtension())) //Excludes non-txt files
            .map(txtFile -> computeBlocking(() -> PsiManager.getInstance(project).findFile(txtFile)))
            .filter(file -> file instanceof IniFile)
            .map(file -> (IniFile) file)
            .toList();
    }

    private TranslationFilesCollector() {
        //Utility class
    }
}
