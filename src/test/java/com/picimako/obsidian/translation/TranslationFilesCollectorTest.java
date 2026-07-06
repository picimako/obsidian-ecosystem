package com.picimako.obsidian.translation;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.obsidian.translation.lang.IniLanguageSubstitutorKt;

/**
 * Integration test for {@link TranslationFilesCollector}.
 */
public final class TranslationFilesCollectorTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/collecttranslationfiles";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IniLanguageSubstitutorKt.applyIniFileTypeOverride(getProject());
    }

    public void testCollectsTranslationFilesIncludingTheFileAtInvocation() {
        myFixture.copyFileToProject("translations/en.txt");
        myFixture.copyFileToProject("translations/ja.txt");
        myFixture.copyFileToProject("translations/pl.md");
        myFixture.copyDirectoryToProject("translations/somedirectory", "translations/somedirectory");

        var translationFiles = TranslationFilesCollector.collectAppTranslationFiles(ProjectUtil.guessProjectDir(getProject()), getProject());

        assertThat(translationFiles)
            .extracting(PsiFileSystemItem::getName)
            .containsExactlyInAnyOrder("en.txt", "ja.txt");
    }
}
