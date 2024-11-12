package com.picimako.obsidian.plugin.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.obsidian.plugin.ManifestDataCache;
import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;
import com.picimako.obsidian.InspectionTestBase;

/**
 * Integration test for {@link CommandIdInspection}.
 */
public final class CommandIdInspectionTest extends InspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CommandIdInspection();
    }

    public void testNoHighlightingInNonPluginProject() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.NONE;
        ManifestDataCache.getInstance(getProject()).getManifest().setId("my-plugin");

        doTest("main.ts", """
            class MyPlugin extends Plugin {
                async onload() {
                    //non-addCommand() call
                    this.addSettingTab({ id: 'my-plugin-some-great-command' })
            
                    //non-id property
                    this.addCommand({ name: 'my-plugin-some-great-command' });
            
                    //no plugin id prefix
                    this.addCommand({ id: 'some-great-command' });
            
                    //plugin id prefix
                    this.addCommand({ id: 'my-plugin-some-great-command' });
                }
            }
            
            abstract class Plugin {
                addCommand(command: object): void;
                addSettingTab(settingTab: object): void;
            }
            """);
    }

    public void testNoHighlightingInThemeProject() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.THEME;
        ManifestDataCache.getInstance(getProject()).getManifest().setId("my-plugin");

        doTest("main.ts", """
            class MyPlugin extends Plugin {
                async onload() {
                    //non-addCommand() call
                    this.addSettingTab({ id: 'my-plugin-some-great-command' })
            
                    //non-id property
                    this.addCommand({ name: 'my-plugin-some-great-command' });
            
                    //no plugin id prefix
                    this.addCommand({ id: 'some-great-command' });
            
                    //plugin id prefix
                    this.addCommand({ id: 'my-plugin-some-great-command' });
                }
            }
            
            abstract class Plugin {
                addCommand(command: object): void;
                addSettingTab(settingTab: object): void;
            }
            """);
    }

    public void testHighlighting() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;
        ManifestDataCache.getInstance(getProject()).getManifest().setId("my-plugin");

        doTest("main.ts", """
            class MyPlugin extends Plugin {
                async onload() {
                    //non-addCommand() call
                    this.addSettingTab({ id: 'my-plugin-some-great-command' })
            
                    //non-id property
                    this.addCommand({ name: 'my-plugin-some-great-command' });
            
                    //no plugin id prefix
                    this.addCommand({ id: 'some-great-command' });
            
                    //plugin id prefix
                    this.addCommand({ id: <error descr="Command ids must not be prefixed with the plugin id: my-plugin. Obsidian performs that automatically.">'my-plugin-some-great-command'</error> });
                }
            }
            
            abstract class Plugin {
                addCommand(command: object): void;
                addSettingTab(settingTab: object): void;
            }
            """);
    }
}
