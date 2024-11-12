package com.picimako.obsidian;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Base class for testing inspections.
 */
public abstract class InspectionTestBase extends BasePlatformTestCase {

    /**
     * Override this to configure the inspection to be tested.
     */
    protected abstract InspectionProfileEntry getInspection();

    /**
     * Tests highlighting for the pre-configured inspection against a file which is created on-the-fly with the provided name and text.
     *
     * @param filename the filename to create for this test. Must include the file extension.
     * @param text     the text to put into the test file
     */
    protected void doTest(String filename, String text) {
        myFixture.configureByText(filename, text);
        myFixture.enableInspections(getInspection());
        myFixture.testHighlighting(true, false, true);
    }
}
