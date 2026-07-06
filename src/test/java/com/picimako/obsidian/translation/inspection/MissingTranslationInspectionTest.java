package com.picimako.obsidian.translation.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.obsidian.InspectionTestBase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.TranslationReader;
import com.picimako.obsidian.translation.lang.IniLanguageSubstitutorKt;

/**
 * Integration test for {@link MissingTranslationInspection}.
 */
public final class MissingTranslationInspectionTest extends InspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/inspection/translationisoriginal";
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new MissingTranslationInspection();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IniLanguageSubstitutorKt.applyIniFileTypeOverride(getProject());
    }

    public void testNoHighlightingInEnTxt() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);

        doTest("translations/en-no-highlight.txt");
    }

    public void testNoHighlightingInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);
        myFixture.copyFileToProject("translations/en.txt");
        TranslationReader.readOriginal(getProject());

        doTest("translations/hu-no-highlight.txt");
    }

    public void testHighlighting() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);
        myFixture.copyFileToProject("translations/en.txt");
        TranslationReader.readOriginal(getProject());

        doTest("translations/hu.txt");
    }
}
