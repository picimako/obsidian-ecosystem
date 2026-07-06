package com.picimako.obsidian.translation.intention;

import static com.intellij.openapi.application.ReadAction.computeBlocking;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.picimako.obsidian.messages.ObsidianBundle.message;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Iconable;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiFile;
import com.intellij.ui.list.TargetPopup;
import com.intellij.util.IncorrectOperationException;
import com.picimako.obsidian.translation.TranslationFilesCollector;
import ini4idea.lang.psi.IniFile;
import ini4idea.lang.psi.IniSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Locale;
import java.util.Objects;

/**
 * It provides a way to navigate to a property (by its path) in a selected translation file.
 * <p>
 * This is so that users can easily check the translation for a selected property in another language
 * by navigating to it.
 * <p>
 * This intention assumes that the original English file {@code en.json} is present, as well as
 * other translations files too, thus it doesn't report an error if a file is missing.
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
final class ShowTranslationInOtherLanguageIntention implements IntentionAction {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (isProjectObsidianTranslations(project) && file instanceof IniFile) {
            //If the caret is inside a section
            var parentSection = getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), IniSection.class);
            if (parentSection == null) return false;

            var sectionName = parentSection.getIniSectionName();
            return sectionName != null;
        }

        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        var projectRootDir = ProjectUtil.guessProjectDir(project);
        if (projectRootDir == null) return;

        var parentSection = getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), IniSection.class);
        if (parentSection == null) return;

        var translationFiles = TranslationFilesCollector.collectAppTranslationFiles(projectRootDir, project);

        //Display the list of available translation files to navigate to and let the user select one
        var step = new BaseListPopupStep<>(message("select.translation.file"), translationFiles) {
            @Override
            public @Nullable PopupStep<?> onChosen(IniFile selectedTranslationFile, boolean finalChoice) {
                return doFinalStep(() -> handleSelectedFile(parentSection, selectedTranslationFile));
            }
        };

        JBPopupFactory.getInstance().createListPopup(
            project,
            step,
            __ -> TargetPopup.createTargetPresentationRenderer(item -> computeBlocking(() -> {
                var iniFile = (IniFile) item;
                /*
                 * Uses custom cell rendering for when the user is presented with a list of translation files:
                 * [filename] (localized language name of translation)
                 *
                 * For example, 'hu.txt (Magyar)'.
                 */
                return TargetPresentation
                    .builder(iniFile.getName())
                    .containerText(Locale.forLanguageTag(iniFile.getVirtualFile().getNameWithoutExtension()).getDisplayLanguage())
                    .icon(iniFile.getIcon(Iconable.ICON_FLAG_VISIBILITY))
                    .presentation();
            }))).showInBestPositionFor(editor);
    }

    @VisibleForTesting
    void handleSelectedFile(IniSection parentSection, IniFile selectedTranslationFile) {
        var sectionName = computeBlocking(parentSection::getNameText);
        for (var child : computeBlocking(selectedTranslationFile::getChildren)) {
            if (child instanceof IniSection section && Objects.equals(computeBlocking(section::getNameText), sectionName)) {
                section.navigate(true);
                return;
            }
        }
    }

    //Intention action configuration

    @Override
    public @IntentionName @NotNull String getText() {
        return message("show.translation.in.other.language");
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return message("show.translation.in.other.language");
    }
}
