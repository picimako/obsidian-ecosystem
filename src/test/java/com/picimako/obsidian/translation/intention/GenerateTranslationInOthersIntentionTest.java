package com.picimako.obsidian.translation.intention;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiManager;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;

/**
 * Integration test for {@link GenerateTranslationInOthersIntention}.
 */
public final class GenerateTranslationInOthersIntentionTest extends IntentionTestBase {

    @Override
    protected IntentionAction getIntention() {
        return new GenerateTranslationInOthersIntention();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/intention/generatetranslation";
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

    public void testGeneratesTopLevelStringProperty() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                }
              },
              "newProperty": "val<caret>ue"
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto"
                }
              },
              "newProperty": "value"
            }""");
    }

    public void testGeneratesOneLevelDeepStringProperty() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                }
              },
              "new": {
                "property": "val<caret>ue"
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto"
                }
              },
              "new": {
                "property": "value"
              }
            }""");
    }

    public void testGeneratesTwoLevelDeepStringProperty() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "new": {
                "object": {
                  "property": "val<caret>ue"
                }
              },
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor"
                }
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "new": {
                "object": {
                  "property": "value"
                }
              },
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto"
                }
              }
            }""");
    }

    public void testGeneratesPropertyFromFirstPropertyInObject() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "newProperty": "val<caret>ue",
                  "name": "Editor"
                }
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "newProperty": "value",
                  "name": "Szerkeszto"
                }
              }
            }""");
    }

    public void testGeneratesPropertyFromFirstPropertyInObjectWithNoMatchingNextSibling() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "newProperty": "val<caret>ue",
                  "non-matching": "Editor"
                }
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "newProperty": "value",
                  "name": "Szerkeszto"
                }
              }
            }""");
    }

    public void testGeneratesPropertyFromMiddlePropertyInObject() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("it.json", "it.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor",
                  "newProperty": "val<caret>ue",
                  "section-display": "Display"
                }
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Opzioni",
                "editor": {
                  "name": "Editor",
                  "newProperty": "value",
                  "section-display": "Visualizzazione"
                }
              }
            }""");
    }

    public void testGeneratesPropertyFromLastPropertyInObject() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
                "editor": {
                  "name": "Editor",
                  "newProperty": "val<caret>ue"
                }
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "name": "Szerkeszto",
                  "newProperty": "value"
                }
              }
            }""");
    }

    public void testGeneratesPropertyFromLastPropertyInObjectWithNoMatchingPreviousSibling() {
        setProjectAsObsidianTranslations();

        var huJson = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("hu.json", "hu.json"));
        var enJson = myFixture.configureByText("en.json", """
            {
              "setting": {
                "options": "Options",
              "editor": {
                "non-matching": "Editor",
                "newProperty": "val<caret>ue"
              }
              }
            }
            """);

        getIntention().invoke(getProject(), myFixture.getEditor(), enJson);

        assertThat(huJson.getText()).isEqualTo("""
            {
              "setting": {
                "options": "Beallitasok",
                "editor": {
                  "newProperty": "value",
                  "name": "Szerkeszto"
                }
              }
            }""");
    }
}
