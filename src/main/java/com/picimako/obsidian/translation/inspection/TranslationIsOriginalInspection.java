package com.picimako.obsidian.translation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.options.OptPane;
import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.OrderedSet;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.picimako.obsidian.messages.ObsidianBundle.message;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.translation.TranslationFileUtil.getPropertyPath;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;

/**
 * Reports localization entries that have the same value as their original, English entries.
 * <p>
 * Helps with identifying not yet translated strings.
 */
class TranslationIsOriginalInspection extends LocalInspectionTool {
    /**
     * There may be certain properties that do not need translation, or is not allowed (e.g. brand names), thus
     * they are excluded from reporting.
     */
    public OrderedSet<String> ignoredProperties = new OrderedSet<>(List.of(
        "setting.account.label-vip",
        "editor.print-modal.setting-page-size-a3",
        "editor.print-modal.setting-page-size-a4",
        "editor.print-modal.setting-page-size-a5",
        "interface.start-screen.mobile.label-obsidian-sync",
        "interface.start-screen.mobile.label-icloud",
        "interface.start-screen.mobile.icloud-missing",
        "plugins.backlinks.ellipsis",
        "plugins.bookmarks.option-u-r-l",
        "plugins.bases.placeholder-formula",
        "plugins.bases.label-sort-a-z",
        "plugins.bases.label-sort-z-a",
        "plugins.bases.label-sort01",
        "plugins.bases.label-sort10",
        "nouns.count"
    ));

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (!isProjectObsidianTranslations(holder.getProject()) || ORIGINAL_LOCALIZATION_FILE.equals(session.getFile().getName()))
            return PsiElementVisitor.EMPTY_VISITOR;

        return new JsonElementVisitor() {
            @Override
            public void visitProperty(@NotNull JsonProperty property) {
                if (property.getValue() instanceof JsonStringLiteral literal) {
                    var propertyPath = getPropertyPath(property, session.getFile());
                    if (ignoredProperties.contains(propertyPath)) return;

                    String originalValueAtPath = OriginalLocalizationValuesCache.getInstance(holder.getProject()).getOriginalValues().get(propertyPath);

                    if (Objects.equals(originalValueAtPath, literal.getValue()))
                        holder.registerProblem(literal, message("translations.is.same.as.original", ProblemHighlightType.WARNING));
                }
            }
        };
    }

    @Override
    public @NotNull OptPane getOptionsPane() {
        return OptPane.pane(OptPane.stringList("ignoredProperties", message("translations.is.same.as.original.options.ignored")));
    }
}