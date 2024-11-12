package com.picimako.obsidian.translation;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiFileSystemItem;
import com.picimako.obsidian.ObsidianTestBase;

/**
 * Integration test for {@link TranslationFilesCollector}.
 */
public final class TranslationFilesCollectorTest extends ObsidianTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/collecttranslationfiles";
    }

    public void testCollectsTranslationFilesIncludingTheFileAtInvocation() {
        myFixture.copyFileToProject(".eslintrc.json");
        myFixture.copyFileToProject("package.json");
        myFixture.copyFileToProject("en.json");
        myFixture.copyFileToProject("ja.json");
        myFixture.copyFileToProject("pl.md");
        myFixture.copyDirectoryToProject("somedirectory", "somedirectory");
        var fileAtInvocation = myFixture.configureByFile("hu.json");

        var translationFiles = TranslationFilesCollector.collectTranslationFiles(ProjectUtil.guessProjectDir(getProject()), getProject(), fileAtInvocation, true);

        assertThat(translationFiles)
            .extracting(PsiFileSystemItem::getName)
            .containsExactlyInAnyOrder("en.json", "hu.json", "ja.json");
    }

    public void testCollectsTranslationFilesExcludingTheFileAtInvocation() {
        myFixture.copyFileToProject(".eslintrc.json");
        myFixture.copyFileToProject("package.json");
        myFixture.copyFileToProject("en.json");
        myFixture.copyFileToProject("ja.json");
        myFixture.copyFileToProject("pl.md");
        myFixture.copyDirectoryToProject("somedirectory", "somedirectory");
        var fileAtInvocation = myFixture.configureByFile("hu.json");

        var translationFiles = TranslationFilesCollector.collectTranslationFiles(ProjectUtil.guessProjectDir(getProject()), getProject(), fileAtInvocation);

        assertThat(translationFiles)
            .extracting(PsiFileSystemItem::getName)
            .containsExactlyInAnyOrder("en.json", "ja.json");
    }
}
