package com.picimako.obsidian.translation.intention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;

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

    //Availability

    public void testNotAvailableInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);

        checkIntentionIsNotAvailable("hu.json", """
            {
            	"setting": {
            		"options": "Beállítások",
            		"editor": {
            			"name": "Szer<caret>kesztő"
            		}
            	}
            }
            """);
    }

    public void testNotAvailableInNonJsonFile() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("hu.md", "<caret>");
    }

    public void testNotAvailableInPackageJson() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("package.json", "<caret>");
    }

    public void testNotAvailableInEslintrcJson() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable(".eslintrc.json", "<caret>");
    }

    public void testNotAvailableForElementWithoutParentProperty() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("hu.json", """
            {<caret>
            	"setting": {
            		"options": "Beállítások",
            		"editor": {
            			"name": "Szerkesztő"
            		}
            	}
            }
            """);
    }

    public void testNotAvailableForNonStringValueParentProperty() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("hu.json", """
            {
            	"setting": {<caret>
            		"options": "Beállítások",
            		"editor": {
            			"name": "Szerkesztő"
            		}
            	}
            }
            """);
    }

    public void testAvailable() {
        setProjectAsObsidianTranslations();

        checkIntentionIsAvailable("hu.json", """
            {
            	"setting": {
            		"options": "Beállítások",
            		"editor": {
            			"name": "Sz<caret>erkesztő"
            		}
            	}
            }
            """);
    }

    //Invocation

    public void testThrowsErrorWhenPropertyIsNotFoundInSelectedFileDueToMissingProperty() {
        setProjectAsObsidianTranslations();

        var selectedFile = myFixture.copyFileToProject("en.json");
        var selectedPsiFile = PsiManager.getInstance(getProject()).findFile(selectedFile);

        var psiFile = myFixture.configureByText("hu.json", """
            {
            	"setting": {
            		"options": "Beállítások",
            		"editor": {
            			"non-existent": "S<caret>zerkesztő"
            		}
            	}
            }
            """);
        var parentProperty = PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.getCaretOffset()), JsonProperty.class);

        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> new ShowTranslationInOtherLanguageIntention()
                .handleSelectedFile(parentProperty, selectedPsiFile, getProject(), myFixture.getEditor(), psiFile))
            .withMessage("This property <code>setting.editor.non-existent</code> is not present in <code>en.json</code>.");
    }

    public void testThrowsErrorWhenPropertyIsNotFoundInSelectedFileDueToMissingParent() {
        setProjectAsObsidianTranslations();

        var selectedFile = myFixture.copyFileToProject("en.json");
        var selectedPsiFile = PsiManager.getInstance(getProject()).findFile(selectedFile);

        var psiFile = myFixture.configureByText("hu.json", """
            {
                	"setting": {
                		"options": "Beállítások",
                		"non-existent": {
                			"name": "Sz<caret>erkesztő"
                		}
                	}
                }
            """);
        var parentProperty = PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.getCaretOffset()), JsonProperty.class);

        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> new ShowTranslationInOtherLanguageIntention()
                .handleSelectedFile(parentProperty, selectedPsiFile, getProject(), myFixture.getEditor(), psiFile))
            .withMessage("This property <code>setting.non-existent.name</code> is not present in <code>en.json</code>.");
    }

    public void testNavigatesToMatchingProperty() {
        setProjectAsObsidianTranslations();

        var selectedFile = myFixture.copyFileToProject("en.json");
        var selectedPsiFile = PsiManager.getInstance(getProject()).findFile(selectedFile);
        var fileEditorManager = FileEditorManager.getInstance(getProject());
        assertThat(fileEditorManager.isFileOpen(selectedFile)).isFalse();

        var psiFile = myFixture.configureByText("hu.json", """
            {
                "setting": {
                    "options": "Beállítások",
                    "editor": {
                        "name": "Sz<caret>erkesztő"
                    }
                }
            }
            """);
        var parentProperty = PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.getCaretOffset()), JsonProperty.class);

        new ShowTranslationInOtherLanguageIntention()
            .handleSelectedFile(parentProperty, selectedPsiFile, getProject(), myFixture.getEditor(), psiFile);

        assertThat(fileEditorManager.isFileOpen(selectedFile)).isTrue();
        assertThat(fileEditorManager.getSelectedTextEditor().getCaretModel().getOffset()).isEqualTo(65);
    }
}
