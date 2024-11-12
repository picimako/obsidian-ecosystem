package com.picimako.obsidian.plugin.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;
import com.picimako.obsidian.InspectionTestBase;

/**
 * Integration test for {@link PluginManifestInspection}.
 */
public final class PluginManifestInspectionTest extends InspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new PluginManifestInspection();
    }

    public void testNoHighlightingInNonManifestJson() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTest("non_manifest.json", """
            {
                "description": "Not ending with dot",
                "description": "Ending with dot.",
                "description": "Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Ex.",
                "description": "Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250."
            }
            """);
    }

    public void testNoHighlightingInNonPluginProject() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.NONE;

        doTest("manifest.json", """
            {
                "description": "Not ending with dot",
                "description": "Ending with dot.",
                "description": "Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Ex.",
                "description": "Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250."
            }
            """);
    }

    public void testHighlightingInPlugin() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTest("manifest.json", """
            {
                "description": <error descr="The plugin description must end with a dot.">"Not ending with dot"</error>,
                "description": "Ending with dot.",
                "description": "Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Exactly 250. Ex.",
                "description": <error descr="The plugin description is 254 characters long. It must be 250 maximum.">"Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250. Longer than 250."</error>
            }
            """);
    }

    public void testHighlightingInThem() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.THEME;

        doTest("manifest.json", """
            {
                "name": "A beautiful theme",
                "name": <warning descr="If the project is stored under Obsidian's 'themes' directory, the directory name must be equal to the theme name.">"A not so beautiful theme"</warning>
            }
            """);
    }
}
