package com.picimako.obsidian.translation.intention;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.translation.TranslationFileUtil.ESLINTRC_JSON;
import static com.picimako.obsidian.translation.TranslationFileUtil.PACKAGE_JSON;
import static com.picimako.obsidian.translation.TranslationFileUtil.getTopLevelObjectOf;
import static com.picimako.obsidian.messages.ObsidianBundle.message;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.TextWithIcon;
import com.picimako.obsidian.translation.TranslationFileUtil;
import com.picimako.obsidian.translation.TranslationFilesCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Locale;

/**
 * It provides a way to navigate to a property (by its path) in a selected translation file.
 * <p>
 * This is so that users can easily check the translation for a selected property in another language
 * by navigating to it.
 * <p>
 * This intention assumes that the original English file {@code en.json} is present, as well as
 * other translations files too, thus it doesn't report an error if a file is missing.
 */
final class ShowTranslationInOtherLanguageIntention implements IntentionAction {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (isProjectObsidianTranslations(project) && file instanceof JsonFile) {
            String fileName = file.getName();
            if (fileName.equals(PACKAGE_JSON) || fileName.equals(ESLINTRC_JSON)) return false;

            var parentProperty = getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), JsonProperty.class);
            return parentProperty != null && parentProperty.getValue() instanceof JsonStringLiteral;
        }

        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        var projectRootDir = ProjectUtil.guessProjectDir(project);
        if (projectRootDir == null) return;

        var parentProperty = getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), JsonProperty.class);
        if (parentProperty == null || !(parentProperty.getValue() instanceof JsonStringLiteral)) return;

        var translationFiles = TranslationFilesCollector.collectTranslationFiles(projectRootDir, project, psiFile);

        //Display the list of available translation files to navigate to and let the user select one
        var step = new BaseListPopupStep<>(message("select.translation.file"), translationFiles) {
            @Override
            public @Nullable PopupStep<?> onChosen(PsiFile selectedTranslationFile, boolean finalChoice) {
                return handleSelectedFile(parentProperty, selectedTranslationFile, project, editor, psiFile);
            }
        };
        JBPopupFactory.getInstance().createListPopup(project, step, __ -> new TranslationFileCellRenderer()).showInBestPositionFor(editor);
    }

    @VisibleForTesting
    PopupStep<?> handleSelectedFile(JsonProperty parentProperty, PsiFile selectedTranslationFile, Project project, Editor editor, PsiFile psiFile) {
        //Get the path of the property as list of property names leading to it
        var propertyPath = TranslationFileUtil.getPropertyPath(parentProperty, psiFile);

        //Traverse the properties collected, in the selected translation file, from top to bottom,
        // until the selected property is reached.
        //Start with a top-level property
        var property = getTopLevelObjectOf(selectedTranslationFile).findProperty(propertyPath.get(0));
        if (property == null) return showErrorHint(selectedTranslationFile, propertyPath, project, editor);

        for (int i = 1; i < propertyPath.size(); i++) {
            //Otherwise, if the currently processed property's value is an object, we cannot process
            // deeper level properties
            if (!(property.getValue() instanceof JsonObject objectValue))
                return showErrorHint(selectedTranslationFile, propertyPath, project, editor);

            //Go one level deeper in the property hierarchy
            property = objectValue.findProperty(propertyPath.get(i));
            if (property == null) return showErrorHint(selectedTranslationFile, propertyPath, project, editor);

            /*
             * If we are processing the last property name, we reached the property, therefore let's navigate to it
             * This check has to stay at the end of this code block, otherwise the navigation would happen to the parent
             *  property, even if the selected property is not present.
             */
            if (i == propertyPath.size() - 1) {
                property.navigate(true);
                return null;
            }
        }

        return null;
    }

    @Nullable
    private PopupStep<?> showErrorHint(PsiFile selectedTranslationFile, List<String> pathElements, @NotNull Project project, Editor editor) {
        CommonRefactoringUtil.showErrorHint(project, editor,
            message("property.not.present.in.translation.file", String.join(".", pathElements), selectedTranslationFile.getName()),
            message("could.not.find.property"),
            "");
        return null;
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
        return "Show translation in other language";
    }

    /**
     * Custom cell renderer for when the user is presented with a list of translation files.
     * The cell is rendered as:
     * <pre>
     * {@code
     *   [filename] (localized language name of translation)
     *   for example:
     *   hu.json (Magyar)
     * }
     * </pre>
     */
    private static final class TranslationFileCellRenderer extends PsiElementListCellRenderer<JsonFile> {
        public TranslationFileCellRenderer() {
        }

        protected int getIconFlags() {
            return 1;
        }

        //Left side portion
        public String getElementText(JsonFile element) {
            return element.getName();
        }

        //Text in parentheses after the element text
        public String getContainerText(JsonFile element, String name) {
            Locale locale = Locale.forLanguageTag(element.getVirtualFile().getNameWithoutExtension());
            return locale.getDisplayLanguage();
        }

        //Right side portion
        @Override
        protected @Nullable TextWithIcon getItemLocation(Object value) {
            return null;
        }
    }
}
