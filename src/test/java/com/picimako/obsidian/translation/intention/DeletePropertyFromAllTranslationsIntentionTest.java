package com.picimako.obsidian.translation.intention;

import static com.picimako.obsidian.translation.TranslationFileUtil.findPropertyByPath;
import static com.picimako.obsidian.translation.TranslationFileUtil.getTopLevelObjectOf;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiManager;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;

import java.util.List;

/**
 * Integration test for {@link DeletePropertyFromAllTranslationsIntention}.
 */
public final class DeletePropertyFromAllTranslationsIntentionTest extends IntentionTestBase {

    @Override
    protected IntentionAction getIntention() {
        return new DeletePropertyFromAllTranslationsIntention();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/intention/cleanuptranslation";
    }

    //Availability

    public void testNotAvailableInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);

        checkIntentionIsNotAvailable("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Edit<caret>or"
                }
              }
            }
            """);
    }

    public void testNotAvailableInNonJsonFile() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("en.md", "<caret>");
    }

    public void testNotAvailableInNonEnJson() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("package.json", "<caret>");
    }

    public void testNotAvailableForElementWithoutParentProperty() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("en.json", """
            {<caret>
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                }
              }
            }
            """);
    }

    public void testNotAvailableForNonStringValueParentProperty() {
        setProjectAsObsidianTranslations();

        checkIntentionIsNotAvailable("en.json", """
            {
              "setting": {<caret>
                "options": "Options",
                "editor": {
                  "name": "Editor"
                }
              }
            }
            """);
    }

    public void testAvailable() {
        setProjectAsObsidianTranslations();

        checkIntentionIsAvailable("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Edit<caret>or"
                }
              }
            }
            """);
    }

    //Invocation

    public void testCleansUpSinglePropertyInObject() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByFile("en.json");
        var subjectProperty = findPropertyByPath(List.of("setting", "editor", "name"), getTopLevelObjectOf(enJson));
        myFixture.getEditor().getCaretModel().moveToOffset(subjectProperty.getTextOffset());

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(enJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Options",
                "editor": {
                 \s
                },
                "file": {
                  "name": "Files & Links",
                  "option-confirm-file-deletion": "Confirm file deletion"
                },
                "appearance": {
                  "name": "Appearance",
                  "option-base-theme": "Base theme",
                  "option-base-theme-description": "Choose Obsidian's default color scheme."
                }
              }
            }""");
        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                 \s
                },
                "file": {
                  "name": "Fajlok es hivatkozasok",
                  "option-confirm-file-deletion": "Fajl torlesenek megerositese"
                },
                "appearance": {
                  "name": "Megjelenes",
                  "option-base-theme": "Alap tema",
                  "option-base-theme-description": "Valassza ki az Obsidian alapertelmezett szinsemajat."
                }
              }
            }
            """);
    }

    public void testCleansUpPropertyInObjectWithOnlyPreviousSibling() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByFile("en.json");
        var subjectProperty = findPropertyByPath(List.of("setting", "file", "option-confirm-file-deletion"), getTopLevelObjectOf(enJson));
        myFixture.getEditor().getCaretModel().moveToOffset(subjectProperty.getTextOffset());

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(enJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                },
                "file": {
                  "name": "Files & Links"
                },
                "appearance": {
                  "name": "Appearance",
                  "option-base-theme": "Base theme",
                  "option-base-theme-description": "Choose Obsidian's default color scheme."
                }
              }
            }""");
        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto"
                },
                "file": {
                  "name": "Fajlok es hivatkozasok"
                },
                "appearance": {
                  "name": "Megjelenes",
                  "option-base-theme": "Alap tema",
                  "option-base-theme-description": "Valassza ki az Obsidian alapertelmezett szinsemajat."
                }
              }
            }
            """);
    }

    public void testCleansUpPropertyInObjectWithOnlyNextSibling() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByFile("en.json");
        var subjectProperty = findPropertyByPath(List.of("setting", "file", "name"), getTopLevelObjectOf(enJson));
        myFixture.getEditor().getCaretModel().moveToOffset(subjectProperty.getTextOffset());

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(enJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                },
                "file": {
                  "option-confirm-file-deletion": "Confirm file deletion"
                },
                "appearance": {
                  "name": "Appearance",
                  "option-base-theme": "Base theme",
                  "option-base-theme-description": "Choose Obsidian's default color scheme."
                }
              }
            }""");
        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto"
                },
                "file": {
                  "option-confirm-file-deletion": "Fajl torlesenek megerositese"
                },
                "appearance": {
                  "name": "Megjelenes",
                  "option-base-theme": "Alap tema",
                  "option-base-theme-description": "Valassza ki az Obsidian alapertelmezett szinsemajat."
                }
              }
            }
            """);
    }

    public void testCleansUpPropertyInObjectWithBothPreviousAndNextSiblings() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByFile("en.json");
        var subjectProperty = findPropertyByPath(List.of("setting", "appearance", "option-base-theme"), getTopLevelObjectOf(enJson));
        myFixture.getEditor().getCaretModel().moveToOffset(subjectProperty.getTextOffset());

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(enJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                },
                "file": {
                  "name": "Files & Links",
                  "option-confirm-file-deletion": "Confirm file deletion"
                },
                "appearance": {
                  "name": "Appearance",
                  "option-base-theme-description": "Choose Obsidian's default color scheme."
                }
              }
            }""");
        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto"
                },
                "file": {
                  "name": "Fajlok es hivatkozasok",
                  "option-confirm-file-deletion": "Fajl torlesenek megerositese"
                },
                "appearance": {
                  "name": "Megjelenes",
                  "option-base-theme-description": "Valassza ki az Obsidian alapertelmezett szinsemajat."
                }
              }
            }
            """);
    }
}
