package com.picimako.obsidian.inlay;

import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.getOriginalValueAt;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.translation.TranslationFileUtil.getPropertyPath;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;

/**
 * Adds inlay hints above all localization properties with their original, English values.
 */
@SuppressWarnings("UnstableApiUsage")
class OriginalValueInlayHintsProvider implements InlayHintsProvider {
    private static final SharedBypassCollector NO_OP_COLLECTOR = (host, sink) -> {
    };
    private static final String WHITESPACE = " ";
    private static final String DELIMITER = "''";

    @Override
    public @Nullable InlayHintsCollector createCollector(@NotNull PsiFile psiFile, @NotNull Editor editor) {
        if (!(psiFile instanceof JsonFile)
            || !isProjectObsidianTranslations(psiFile.getProject())
            || ORIGINAL_LOCALIZATION_FILE.equals(psiFile.getName()))
            return NO_OP_COLLECTOR;

        return (SharedBypassCollector) (host, sink) -> {
            if (!(host instanceof JsonProperty property)) return;

            var propertyPath = getPropertyPath(property, psiFile);
            Optional.ofNullable(getOriginalValueAt(propertyPath, psiFile.getProject()))
                //If a string starts or ends with a whitespace, wrap it in quotes so that those whitespaces become visible
                // in the inlay hints
                .map(value -> value.startsWith(WHITESPACE) || value.endsWith(WHITESPACE) ? DELIMITER + value + DELIMITER : value)
                //Makes \n symbols appears as literals
                .map(value -> value.replace("\n", "\\n"))
                .ifPresent(valueAtPath -> sink.addPresentation(
                    new AboveLineIndentedPosition(property.getTextOffset(), 0, 0),
                    null,
                    null,
                    HintFormat.Companion.getDefault().withColorKind(HintColorKind.TextWithoutBackground),
                    treeBuilder -> {
                        treeBuilder.text(valueAtPath, null);
                        return Unit.INSTANCE;
                    }));
        };
    }
}
