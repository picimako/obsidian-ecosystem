package com.picimako.obsidian.translation.inspection;

import static com.picimako.obsidian.messages.ObsidianBundle.message;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.getOriginalValueAt;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.translation.TranslationFilesCollector.isOriginalLocFile;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.options.OptPane;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.OrderedSet;
import ini4idea.lang.psi.IniSection;
import ini4idea.lang.psi.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Reports localization entries that have the same value as their original, English entries.
 * <p>
 * Helps with identifying not yet translated strings.
 */
class MissingTranslationInspection extends LocalInspectionTool {
    /**
     * There may be certain properties that do not need translation, or are not allowed (e.g. brand names),
     * thus they are excluded from reporting.
     */
    public OrderedSet<String> ignoredSectionNames = new OrderedSet<>(List.of(
        "[setting.file.label-obsidian-uri]",
        "[interface.mobile.action-insert-text-into-file]",
        "[pdf.annotation_date_string]",
        "[plugins.backlinks.ellipsis]",
        "[plugins.bases.label-sort-a-z]",
        "[plugins.bases.label-sort-z-a]",
        "[plugins.bases.label-sort01]",
        "[plugins.bases.label-sort10]",
        "[main.button-ok]",
        "[nouns.count]"
    ));

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (!isProjectObsidianTranslations(holder.getProject()) || isOriginalLocFile(session.getFile()))
            return PsiElementVisitor.EMPTY_VISITOR;

        return new Visitor() {
            @Override
            public void visitIniSection(@NotNull IniSection section) {
                var sectionName = section.getNameText();
                if (ignoredSectionNames.contains(sectionName)) return;

                var translationValue = section.getIniPropertyList().get(1).getIniValue();
                if (translationValue == null) return;

                String originalValueAtSection = getOriginalValueAt(sectionName, holder.getProject());
                if (Objects.equals(originalValueAtSection, translationValue.getText()))
                    holder.registerProblem(translationValue, message("translation.is.same.as.original"));
            }
        };
    }

    @Override
    public @NotNull OptPane getOptionsPane() {
        return OptPane.pane(OptPane.stringList("ignoredSectionNames", message("translation.is.same.as.original.options.ignored")));
    }
}