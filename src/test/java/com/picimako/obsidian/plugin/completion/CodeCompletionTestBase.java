package com.picimako.obsidian.plugin.completion;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.completion.CompletionType;
import com.picimako.obsidian.ObsidianTestBase;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for testing code completions.
 */
abstract class CodeCompletionTestBase extends ObsidianTestBase {

    /**
     * Tests basic code completion in the provided file {@code text} at the marked caret position,
     * and validates if the completion contains exactly the expected {@code completionItems}.
     */
    protected void doTestCodeCompletion(String fileName, String text, String... completionItems) {
        var lookupElementStrings = configureAndGetLookupItems(fileName, text);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder(completionItems);
    }

    /**
     * Tests basic code completion in the provided file {@code text} at the marked caret position,
     * and validates if there is no completion happening.
     */
    protected void doTestNoCodeCompletion(String fileName, String text) {
        assertThat(configureAndGetLookupItems(fileName, text)).isEmpty();
    }

    @Nullable("When the only item was auto-completed.")
    private List<String> configureAndGetLookupItems(String fileName, String text) {
        myFixture.configureByText(fileName, text);
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }
}
