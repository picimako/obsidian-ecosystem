package com.picimako.obsidian.inlay;

import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public @Nullable InlayHintsCollector createCollector(@NotNull PsiFile psiFile, @NotNull Editor editor) {
        if (!(psiFile instanceof JsonFile)
            || !isProjectObsidianTranslations(psiFile.getProject())
            || ORIGINAL_LOCALIZATION_FILE.equals(psiFile.getName()))
            return NO_OP_COLLECTOR;

        return (SharedBypassCollector) (host, sink) -> {
            if (!(host instanceof JsonProperty property)) return;

            var propertyPath = getPropertyPath(property, psiFile);
            String valueAtPath = OriginalLocalizationValuesCache.getInstance(psiFile.getProject()).getOriginalValues().get(propertyPath);
            if (valueAtPath == null) return;

            sink.addPresentation(
                new AboveLineIndentedPosition(property.getTextOffset(), 0, 0),
                null,
                null,
                HintFormat.Companion.getDefault().withColorKind(HintColorKind.TextWithoutBackground),
                treeBuilder -> {
                    treeBuilder.text(valueAtPath, null);
                    return Unit.INSTANCE;
                });
        };
    }
}
