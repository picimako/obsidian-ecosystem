package com.picimako.obsidian.translation.inspection;

import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.translation.TranslationFileUtil.getPropertyPath;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;
import static com.picimako.obsidian.translation.inspection.VariablesCollector.collectVariablesIn;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.SmartHashSet;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.messages.ObsidianBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Reports missing and invalid {@code {{...}}}-style variables in translation values.
 */
final class VariableUsageInspection extends LocalInspectionTool {
    /**
     * This means that the String value is at least something like this: {@code {{v}}}.
     */
    private static final int STRING_WITH_VARIABLE_MINIMUM_LENGTH = 5;

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (!isProjectObsidianTranslations(holder.getProject()) || ORIGINAL_LOCALIZATION_FILE.equals(session.getFile().getName()))
            return PsiElementVisitor.EMPTY_VISITOR;

        return new JsonElementVisitor() {
            @Override
            public void visitProperty(@NotNull JsonProperty property) {
                if (property.getValue() instanceof JsonStringLiteral literal && literal.getValue().length() >= STRING_WITH_VARIABLE_MINIMUM_LENGTH) {
                    var propertyPath = String.join(".", getPropertyPath(property, session.getFile()));
                    String valueAtPath = OriginalLocalizationValuesCache.getInstance(holder.getProject()).getOriginalValues().get(propertyPath);
                    if (valueAtPath == null) return;

                    //Collect the variables from the English value by the path of the property of the value being inspected
                    var variableInEnglishValue = collectVariablesIn(valueAtPath);
                    var variablesInCurrentValue = collectVariablesIn(literal.getValue());

                    //This prevents the creation of at least the first SmartHashSet instance below
                    if (variableInEnglishValue.isEmpty() && variablesInCurrentValue.isEmpty()) return;

                    checkForMissingVariables(literal, variableInEnglishValue, variablesInCurrentValue, holder);
                    //Checking for invalid variable must be performed only when there is at least one variable in the string
                    if (literal.getValue().contains("{{"))
                        checkForInvalidVariables(literal, variableInEnglishValue, variablesInCurrentValue, holder);
                }
            }

            /**
             * Inspects variables that are present in en.json, but not present in the current translation.
             */
            private static void checkForMissingVariables(JsonStringLiteral literal, Set<String> variablesInEnglishValue, Set<String> variablesInCurrentValue, @NotNull ProblemsHolder holder) {
                var englishVariablesNotInCurrent = new SmartHashSet<String>();
                for (String variableInEnglish : variablesInEnglishValue) {
                    if (!variablesInCurrentValue.contains(variableInEnglish))
                        englishVariablesNotInCurrent.add(variableInEnglish);
                }
                if (!englishVariablesNotInCurrent.isEmpty())
                    holder.registerProblem(literal, ObsidianBundle.message("variable.usage.missing.variables", String.join(", ", englishVariablesNotInCurrent)), ProblemHighlightType.GENERIC_ERROR);
            }

            /**
             * Inspects variables that are not valid based on what the English value contains.
             */
            private static void checkForInvalidVariables(JsonStringLiteral literal, Set<String> variablesInEnglishValue, Set<String> variablesInCurrentValue, @NotNull ProblemsHolder holder) {
                var invalidVariables = new SmartHashSet<String>();
                for (String variableInCurrent : variablesInCurrentValue) {
                    if (!variablesInEnglishValue.contains(variableInCurrent))
                        invalidVariables.add(variableInCurrent);
                }
                if (!invalidVariables.isEmpty())
                    holder.registerProblem(literal,
                        ObsidianBundle.message("variable.usage.invalid.variables", String.join(", ", invalidVariables), String.join(", ", variablesInEnglishValue)),
                        ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}
