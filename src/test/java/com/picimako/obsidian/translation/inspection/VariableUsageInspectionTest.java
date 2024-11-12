package com.picimako.obsidian.translation.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.obsidian.InspectionTestBase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.TranslationReader;

/**
 * Integration test for {@link VariableUsageInspection}.
 */
public final class VariableUsageInspectionTest extends InspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/inspection/variableusage";
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new VariableUsageInspection();
    }

    public void testNoHighlightingInEnJson() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);

        doTest("en.json", """
            {
              "nouns": {
                "character-with-count": "{{cou}} character",
                "word-with-count": "1 word"
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
                "character-with-count": "{{cou}} karakter",
                "word-with-count": "1 szo"
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
                "character-with-count": <error descr="The following variables are invalid: cou. Valid ones are: count"><error descr="The following variables are not used from the English value: count">"{{cou}} karakter"</error></error>,
                "word-with-count": <error descr="The following variables are not used from the English value: size">"1 szo"</error>
              }
            }
            """);
    }
}
