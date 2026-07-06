package com.picimako.obsidian.translation.inspection;

import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.getOriginalValueAt;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.translation.TranslationFilesCollector.isOriginalLocFile;
import static com.picimako.obsidian.translation.inspection.VariablesCollector.collectVariablesIn;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.SmartHashSet;
import com.picimako.obsidian.messages.ObsidianBundle;
import ini4idea.lang.psi.IniSection;
import ini4idea.lang.psi.IniValue;
import ini4idea.lang.psi.Visitor;
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
        if (!isProjectObsidianTranslations(holder.getProject()) || isOriginalLocFile(session.getFile()))
            return PsiElementVisitor.EMPTY_VISITOR;

        return new Visitor() {
            @Override
            public void visitIniSection(@NotNull IniSection section) {
                var translationValue = section.getIniPropertyList().get(1).getIniValue();
                if (translationValue == null) return;

                var translation = translationValue.getText();

                if (translation.length() >= STRING_WITH_VARIABLE_MINIMUM_LENGTH) {
                    var sectionName = section.getNameText();

                    String valueAtSection = getOriginalValueAt(sectionName, holder.getProject());
                    if (valueAtSection == null) return;

                    //Collect the variables from the English value by the path of the property of the value being inspected
                    var variableInEnglishValue = collectVariablesIn(valueAtSection);
                    var variablesInCurrentValue = collectVariablesIn(translation);

                    //This prevents the creation of at least the first SmartHashSet instance below
                    if (variableInEnglishValue.isEmpty() && variablesInCurrentValue.isEmpty()) return;

                    checkForMissingVariables(translationValue, variableInEnglishValue, variablesInCurrentValue, holder);

                    //Checking for invalid variable must be performed only when there is at least one variable in the string
                    if (translation.contains("{{"))
                        checkForInvalidVariables(translationValue, variableInEnglishValue, variablesInCurrentValue, holder);
                }
            }

            /**
             * Inspects variables that are present in en.json, but not present in the current translation.
             */
            private static void checkForMissingVariables(IniValue translationValue, Set<String> variablesInEnglishValue, Set<String> variablesInCurrentValue, @NotNull ProblemsHolder holder) {
                var englishVariablesNotInCurrent = new SmartHashSet<String>();
                for (String variableInEnglish : variablesInEnglishValue) {
                    if (!variablesInCurrentValue.contains(variableInEnglish))
                        englishVariablesNotInCurrent.add(variableInEnglish);
                }
                if (!englishVariablesNotInCurrent.isEmpty())
                    holder.registerProblem(translationValue, ObsidianBundle.message("variable.usage.missing.variables", String.join(", ", englishVariablesNotInCurrent)), ProblemHighlightType.GENERIC_ERROR);
            }

            /**
             * Inspects variables that are not valid based on what the English value contains.
             */
            private static void checkForInvalidVariables(IniValue translationValue, Set<String> variablesInEnglishValue, Set<String> variablesInCurrentValue, @NotNull ProblemsHolder holder) {
                var invalidVariables = new SmartHashSet<String>();
                for (String variableInCurrent : variablesInCurrentValue) {
                    if (!variablesInEnglishValue.contains(variableInCurrent))
                        invalidVariables.add(variableInCurrent);
                }
                if (!invalidVariables.isEmpty())
                    holder.registerProblem(translationValue,
                        ObsidianBundle.message("variable.usage.invalid.variables", String.join(", ", invalidVariables), String.join(", ", variablesInEnglishValue)),
                        ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}
