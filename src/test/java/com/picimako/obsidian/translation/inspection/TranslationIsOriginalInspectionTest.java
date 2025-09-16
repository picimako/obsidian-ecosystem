package com.picimako.obsidian.translation.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.obsidian.InspectionTestBase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.TranslationReader;

/**
 * Integration test for {@link TranslationIsOriginalInspection}.
 */
public final class TranslationIsOriginalInspectionTest extends InspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/inspection/translationisoriginal";
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new TranslationIsOriginalInspection();
    }

    public void testNoHighlightingInEnJson() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);

        doTest("en.json", """
            {
              "nouns": {
                "count": "{{count}}",
                "word-with-count": "{{count}} word"
              }
            }
            """);
    }

    public void testHighlightingInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);
        myFixture.copyFileToProject("en.json");
        TranslationReader.readOriginal(getProject());

        doTest("hu.json", """
            {
              "nouns": {
                "count": "{{count}}",
                "word-with-count": "{{count}} szó"
              }
            }
            """);
    }

    public void testHighlighting() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);
        myFixture.copyFileToProject("en.json");
        TranslationReader.readOriginal(getProject());

        doTest("hu.json", """
            {
              "nouns": {
                "count": "{{count}}",
                "word-with-count": "{{count}} szó",
                "word-with-count_plural": <error descr="This entry is not translated.">"{{count}} words"</error>,
              }
            }
            """);
    }
}
