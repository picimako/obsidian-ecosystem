package com.picimako.obsidian.translation.intention;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.lang.IniLanguageSubstitutorKt;
import ini4idea.lang.psi.IniFile;
import ini4idea.lang.psi.IniSection;

/**
 * Integration test for {@link ShowTranslationInOtherLanguageIntention}.
 */
public final class ShowTranslationInOtherLanguageIntentionTest extends IntentionTestBase {

    @Override
    protected IntentionAction getIntention() {
        return new ShowTranslationInOtherLanguageIntention();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/intention/showtranslation";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IniLanguageSubstitutorKt.applyIniFileTypeOverride(getProject());
    }

    //Availability

    public void testNotAvailableInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);

        checkIntentionIsNotAvailable("translations/pl.txt");
    }

    public void testNotAvailableInNonTxtFile() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("translations/hu.md");
    }

    public void testNotAvailableForElementWithoutParentSection() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("translations/jp.txt");
    }

    public void testAvailable() {
        setProjectAsObsidianTranslations();

        checkIntentionIsAvailable("translations/nl.txt");
    }

    //Invocation

    public void testNavigatesToMatchingProperty() {
        setProjectAsObsidianTranslations();

        var selectedFile = myFixture.copyFileToProject("translations/en.txt");
        var selectedPsiFile = (IniFile) PsiManager.getInstance(getProject()).findFile(selectedFile);
        var fileEditorManager = FileEditorManager.getInstance(getProject());
        assertThat(fileEditorManager.isFileOpen(selectedFile)).isFalse();

        var psiFile = myFixture.configureByFile("translations/ru.txt");
        var parentSection = PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.getCaretOffset()), IniSection.class);

        new ShowTranslationInOtherLanguageIntention().handleSelectedFile(parentSection, selectedPsiFile);

        assertThat(fileEditorManager.isFileOpen(selectedFile)).isTrue();
        assertThat(fileEditorManager.getSelectedTextEditor().getCaretModel().getOffset()).isEqualTo(49);
    }
}
