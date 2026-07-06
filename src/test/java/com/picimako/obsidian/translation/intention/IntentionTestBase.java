package com.picimako.obsidian.translation.intention;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;

/**
 * Base class for testing intention actions.
 */
abstract class IntentionTestBase extends BasePlatformTestCase {

    /**
     * Implement this to provide an instance of the test subject intention action.
     */
    protected abstract IntentionAction getIntention();

    /**
     * Validates if the intention is available at the caret position in the provided file content.
     *
     * @param filePath the file name to configure the test data in
     */
    protected void checkIntentionIsAvailable(String filePath) {
        checkIntentionAvailability(filePath, true);
    }

    /**
     * Validates if the intention is not available at the caret position in the provided file content.
     *
     * @param filePath the file name to configure the test data in
     */
    protected void checkIntentionIsNotAvailable(String filePath) {
        checkIntentionAvailability(filePath, false);
    }

    private void checkIntentionAvailability(String filePath, boolean availability) {
        var psiFile = myFixture.configureByFile(filePath);
        assertThat(getIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isEqualTo(availability);
    }

    protected void setProjectAsObsidianTranslations() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);
    }
}
