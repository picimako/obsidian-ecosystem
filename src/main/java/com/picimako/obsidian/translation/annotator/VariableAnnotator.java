package com.picimako.obsidian.translation.annotator;

import static com.intellij.lang.annotation.HighlightSeverity.INFORMATION;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;

import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.picimako.obsidian.translation.TranslationFileUtil;
import com.picimako.obsidian.translation.inspection.VariablesCollector;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

/**
 * Annotates variables (that are in the form of {@code {{variableName}}}) inside JSON String property values.
 */
final class VariableAnnotator implements Annotator {
    /**
     * This means that the String value is at least something like this: {@code "{{v}}"}.
     */
    private static final int STRING_WITH_VARIABLE_MINIMUM_LENGTH = 7;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!isProjectObsidianTranslations(element.getProject())
            || !(element instanceof JsonStringLiteral jsonLiteral)
            || TranslationFileUtil.isPackageOrEslintrcJson(element.getContainingFile().getName())) return;

        //Using the literal value that include the leading and trailing " symbols, so that it is easier to
        // calculate the adjusted ranges.
        String literalTextWithQuotes = jsonLiteral.getText();

        if (literalTextWithQuotes.length() < STRING_WITH_VARIABLE_MINIMUM_LENGTH) return;

        annotate(element, VariablesCollector.VARIABLE_PATTERN.matcher(literalTextWithQuotes), holder);
    }

    private static void annotate(@NotNull PsiElement element, Matcher matcher, @NotNull AnnotationHolder holder) {
        int elementOffset = -1; //to minimize the number of times the text offset is queried
        while (matcher.find()) {
            if (elementOffset == -1) elementOffset = element.getTextOffset();

            int openingBraceOffset = elementOffset + matcher.start();
            int closingBraceOffset = elementOffset + matcher.end() - 2;
            int variableContentStartOffset = openingBraceOffset + 2;

            //The opening {{ symbols
            registerAnnotation(TextRange.create(openingBraceOffset, variableContentStartOffset), KEYWORD, holder);
            //Anything between the opening and closing curly braces
            registerAnnotation(TextRange.create(variableContentStartOffset, closingBraceOffset), DefaultLanguageHighlighterColors.LOCAL_VARIABLE, holder);
            //The closing }} symbols
            registerAnnotation(TextRange.create(closingBraceOffset, closingBraceOffset + 2), KEYWORD, holder);
        }
    }

    /**
     * Creates and registers the actual annotation.
     *
     * @param textRange     the text range that is being annotated
     * @param textAttribute the formatting of the text in the given range
     * @param holder        the holder in/via which the registration happens
     */
    private static void registerAnnotation(TextRange textRange, TextAttributesKey textAttribute, @NotNull AnnotationHolder holder) {
        holder.newSilentAnnotation(INFORMATION)
            .range(textRange)
            .textAttributes(textAttribute)
            .create();
    }
}
