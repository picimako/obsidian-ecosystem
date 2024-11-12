package com.picimako.obsidian.translation;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.obsidian.ObsidianTestBase;

import java.util.Map;

/**
 * Integration test for {@link TranslationReader}.
 */
public final class TranslationReaderTest extends ObsidianTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/originalvalues/reader";
    }

    public void testShouldReadOriginalAndCacheProperties() {
        myFixture.copyFileToProject("en.json");
        var expectedValues = Map.of(
            "properties.types.option-multitext", "List",
            "properties.types.option-checkbox", "Checkbox",
            "properties.value-suggestion.instruction-link-note", "to link note",
            "properties.value-suggestion.instruction-dismiss", "to dismiss",
            "properties.option-property-type", "Property type",
            "properties.msg-empty-property-name", "Property name cannot be empty."
            );

        TranslationReader.readOriginal(getProject());

        var originalValues = OriginalLocalizationValuesCache.getInstance(getProject()).getOriginalValues();
        assertThat(originalValues).containsExactlyInAnyOrderEntriesOf(expectedValues);
    }
}
