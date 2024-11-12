package com.picimako.obsidian.translation.intention;

import static com.intellij.psi.util.PsiTreeUtil.getNextSiblingOfType;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.psi.util.PsiTreeUtil.getPrevSiblingOfType;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for intention actions.
 */
abstract class IntentionActionBase implements IntentionAction {

    //Property retrieval

    /**
     * Returns the JSON property the caret is located at in {@code editor}.
     */
    @Nullable
    protected JsonProperty getSubjectProperty(PsiFile psiFile, Editor editor) {
        var propertyToDelete = getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), JsonProperty.class);
        return propertyToDelete != null && propertyToDelete.getValue() instanceof JsonStringLiteral
            ? propertyToDelete
            : null;
    }

    @Nullable
    protected JsonProperty getPrevSiblingProperty(JsonProperty property) {
        return getPrevSiblingOfType(property, JsonProperty.class);
    }

    @Nullable
    protected JsonProperty getNextSiblingProperty(JsonProperty property) {
        return getNextSiblingOfType(property, JsonProperty.class);
    }

    //Progress indicator

    protected void start(ProgressIndicator indicator, String message) {
        indicator.setIndeterminate(false);
        indicator.setFraction(0);
        indicator.setText(message);
    }

    protected void moveIndicator(ProgressIndicator indicator, List<PsiFile> translationFiles, int fileIndex, String message) {
        ProgressManager.checkCanceled();
        indicator.setFraction((double) translationFiles.size() / fileIndex);
        indicator.setText(message);
    }
}
