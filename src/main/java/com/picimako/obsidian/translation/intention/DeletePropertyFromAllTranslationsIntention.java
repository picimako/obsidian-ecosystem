package com.picimako.obsidian.translation.intention;

import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.TextRanges.endOffsetOf;
import static com.picimako.obsidian.translation.TranslationFileUtil.findPropertyByPath;
import static com.picimako.obsidian.translation.TranslationFileUtil.getPropertyPath;
import static com.picimako.obsidian.translation.TranslationFileUtil.getTopLevelObjectOf;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;
import static com.picimako.obsidian.translation.TranslationFilesCollector.collectTranslationFiles;
import static com.picimako.obsidian.messages.ObsidianBundle.message;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This intention deletes the selected String property in all translation files.
 * <p>
 * The intention is available only in {@code en.json}.
 */
final class DeletePropertyFromAllTranslationsIntention extends IntentionActionBase {

    /**
     * The availability check doesn't check if the selected property is already present in other languages,
     * the actual intention action invocation performs that part.
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return isProjectObsidianTranslations(project)
               && ORIGINAL_LOCALIZATION_FILE.equals(file.getName())
               && getSubjectProperty(file, editor) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        var projectRootDir = ProjectUtil.guessProjectDir(project);
        if (projectRootDir == null) return;

        //E.g. when invoked at "someProperty": "som<caret>eValue", it returns the whole JsonProperty for 'someProperty'
        var propertyToDelete = getSubjectProperty(psiFile, editor);
        if (propertyToDelete == null) return;

        var translationFiles = collectTranslationFiles(projectRootDir, project, psiFile, true);
        var propertyPath = getPropertyPath(propertyToDelete, psiFile);

        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() ->
                    deleteProperty(propertyPath, translationFiles, project, ProgressManager.getInstance().getProgressIndicator()),
                message("clean.up.property.progress.title"), true, project);
        } else {
            deleteProperty(propertyPath, translationFiles, project, null);
        }
    }

    private void deleteProperty(List<String> propertyPath,
                                List<PsiFile> translationFiles,
                                Project project,
                                @Nullable("In tests.") ProgressIndicator indicator) {
        var unitTestMode = ApplicationManager.getApplication().isUnitTestMode();
        if (!unitTestMode) start(indicator, message("clean.up.property.collecting.context.information"));

        for (int i = 0; i < translationFiles.size(); i++) {
            if (!unitTestMode)
                moveIndicator(indicator, translationFiles, i, message("deleting.property.progress.step.title", translationFiles.get(i).getName()));

            var translationFile = translationFiles.get(i);
            var propertyAtPath = findPropertyByPath(propertyPath, getTopLevelObjectOf(translationFile));

            if (propertyAtPath != null) {
                runWriteCommandAction(project, () -> {
                    var psiDocumentManager = PsiDocumentManager.getInstance(project);
                    var translationFileDocument = psiDocumentManager.getDocument(translationFile);
                    if (translationFileDocument == null) return;

                    @Nullable var prevSiblingProperty = getPrevSiblingProperty(propertyAtPath);

                    if (prevSiblingProperty == null) {
                        @Nullable var nextSiblingProperty = getNextSiblingProperty(propertyAtPath);
                        if (nextSiblingProperty == null)
                            //If propertyAtPath is the only one in its parent object.
                            // NOTE: Removing the parent object that becomes empty after this deletion is not performed
                            translationFileDocument.deleteString(propertyAtPath.getTextOffset(), endOffsetOf(propertyAtPath));
                        else
                            //If there is only a next sibling property
                            translationFileDocument.deleteString(propertyAtPath.getTextOffset(), nextSiblingProperty.getTextOffset());

                        psiDocumentManager.commitDocument(translationFileDocument);
                    }
                    //If there is a previous sibling property
                    else {
                        translationFileDocument.deleteString(endOffsetOf(prevSiblingProperty), endOffsetOf(propertyAtPath));
                        psiDocumentManager.commitDocument(translationFileDocument);
                    }
                });
            }
        }
        if (!unitTestMode) indicator.setFraction(1.0); //progress bar and deletion process completed
    }

    //Intention action configuration

    @Override
    public @IntentionName @NotNull String getText() {
        return message("clean.up.property");
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return message("clean.up.property");
    }
}
