package com.picimako.obsidian;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for working with {@link TextRange}s.
 */
public final class TextRanges {

    /**
     * Returns the end offset of the argument element.
     */
    public static int endOffsetOf(@NotNull PsiElement element) {
        return element.getTextRange().getEndOffset();
    }

    private TextRanges() {
        //Utility class
    }
}
