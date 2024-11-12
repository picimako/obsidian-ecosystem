package com.picimako.obsidian.translation.annotator;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;

/**
 * Integration test for {@link VariableAnnotator}.
 */
public final class VariableAnnotatorTest extends BasePlatformTestCase {

    public void testNoAnnotationInNonObsidianTranslationsProject() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(false);

        myFixture.configureByText("en.json", """
            {
              <info descr="Property key">"pdf"</info>: {
                <info descr="Property key">"action-copy-page-link"</info>: "Copy link to page {{page}}",
                <info descr="Property key">"msg-max-search-results"</info>: "{{current}} of over {{limit}} matches",
              }
            }
            """);
        myFixture.testHighlighting(false, true, false);
    }

    public void testNoAnnotationInPackageJson() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);

        myFixture.configureByText("package.json", """
            {
              <info descr="Property key">"name"</info>: "obsidian-{{community}}-releases"
            }
            """);
        myFixture.testHighlighting(false, true, false);
    }

    public void testNoAnnotationInEslintrcJson() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);

        myFixture.configureByText(".eslintrc.json", """
            {
              <info descr="Property key">"extends"</info>: ["plugin:json/recommended"],
              <info descr="Property key">"other-config"</info>: "{{variable}}"
            }
            """);
        myFixture.testHighlighting(false, true, false);
    }

    public void testVariableAnnotations() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);

        myFixture.configureByText("en.json", """
            {
              <info descr="Property key">"pdf"</info>: {
                <info descr="Property key">"action-copy-page-link"</info>: "Copy link to page <info descr="null">{{</info><info descr="null">page</info><info descr="null">}}</info>",
                <info descr="Property key">"msg-max-search-results"</info>: "<info descr="null">{{</info><info descr="null">current</info><info descr="null">}}</info> of over <info descr="null">{{</info><info descr="null">limit</info><info descr="null">}}</info> matches",
              }
            }
            """);
        myFixture.testHighlighting(false, true, false);
    }
}
