package com.picimako.obsidian.translation.annotator;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.lang.IniLanguageSubstitutorKt;

/**
 * Integration test for {@link VariableAnnotator}.
 */
public final class VariableAnnotatorTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/variableannotator";
    }

    public void testNoAnnotationInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);
        IniLanguageSubstitutorKt.applyIniFileTypeOverride(getProject());

        myFixture.copyFileToProject("translations/en.txt");
        myFixture.configureByFile("translations/en.txt");
        myFixture.testHighlighting(false, true, false);
    }

    public void testVariableAnnotations() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);
        IniLanguageSubstitutorKt.applyIniFileTypeOverride(getProject());

        myFixture.copyFileToProject("translations/hu.txt");
        myFixture.configureByFile("translations/hu.txt");
        myFixture.testHighlighting(false, true, false);
    }
}
