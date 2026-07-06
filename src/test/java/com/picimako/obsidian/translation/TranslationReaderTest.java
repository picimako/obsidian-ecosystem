package com.picimako.obsidian.translation;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.obsidian.translation.lang.IniLanguageSubstitutorKt;

import java.util.Map;

/**
 * Integration test for {@link TranslationReader}.
 */
public final class TranslationReaderTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/originalvalues/reader";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IniLanguageSubstitutorKt.applyIniFileTypeOverride(getProject());
    }

    public void testShouldReadOriginalAndCacheProperties() {
        OriginalLocalizationValuesCache.getInstance(getProject()).getOriginalValues().clear();

        myFixture.copyFileToProject("translations/en.txt");
        var expectedValues = Map.of(
            "[properties.types.option-multitext]", "List",
            "[properties.types.option-checkbox]", "Checkbox",
            "[properties.value-suggestion.instruction-link-note]", "to link note",
            "[properties.value-suggestion.instruction-dismiss]", "to dismiss",
            "[properties.option-property-type]", "Property type",
            "[properties.msg-empty-property-name]", "Property name cannot be empty."
            );

        TranslationReader.readOriginal(getProject());

        var originalValues = OriginalLocalizationValuesCache.getInstance(getProject()).getOriginalValues();
        assertThat(originalValues).containsExactlyInAnyOrderEntriesOf(expectedValues);
    }
}
