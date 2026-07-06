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
     * @param filePath the filename to create for this test. Must include the file extension.
     */
    protected void doTest(String filePath) {
        myFixture.configureByFile(filePath);
        myFixture.enableInspections(getInspection());
        myFixture.testHighlighting(true, false, true);
    }
}
