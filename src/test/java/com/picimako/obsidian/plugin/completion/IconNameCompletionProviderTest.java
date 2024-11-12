    package com.picimako.obsidian.plugin.completion;

import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;

    /**
 * Integration test for {@link IconNameCompletionProvider}.
 */
public final class IconNameCompletionProviderTest extends CodeCompletionTestBase {

    public void testNoCompletionInJsFile() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTestNoCodeCompletion("main.js", """
            function setIcon(iconName: string) {
            }
            
            setIcon('dice-<caret>')
            """);
    }

    public void testNoCompletionInNonSupportedFunctionCall() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTestNoCodeCompletion("main.ts", """
            class MyPlugin extends Plugin {
            	async onload() {
            		this.registerView('dice-<caret>', { });
            	}
            }
            
            //Mimics Obsidian's Plugin class
            abstract class Plugin {
                registerView(type: string, viewCreator: object): void;
            }
            """);
    }

    public void testNoCompletionInNonFirstArgumentOfSupportedCall() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTestNoCodeCompletion("main.tsx", """
            class MyPlugin extends Plugin {
            	async onload() {
            		this.addRibbonIcon('someicon', 'dice-<caret>', () => {
            		});
            	}
            }
            
            //Mimics Obsidian's Plugin class
            abstract class Plugin {
                addRibbonIcon(icon: string, title: string, callback: () => any);
            }
            """);
    }

    public void testCompletionInNonPluginSetIcon() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTestCodeCompletion("main.ts", """
                class SomeButton extends ButtonComponent {
                	constructor() {
                		this.setIcon('dic<caret>');
                	}
                }
                
                //Mimics Obsidian's ButtonComponent class
                abstract class ButtonComponent {
                    setIcon(icon: string): this;
                }
                """,
            //Names from LucideIconService
            "dice-1",
            "dice-2",
            "dice-3",
            "dice-4",
            "dice-5",
            "dice-6",
            "dices",
            "align-horizontal-distribute-center",
            "align-vertical-distribute-center",
            //Names from other providers
            "dict");
    }

    public void testCompletionInPluginAddRibbonIcon() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTestCodeCompletion("main.ts", """
                class MyPlugin extends Plugin {
                    async onload() {
                        this.addRibbonIcon('dic<caret>', 'Sample Plugin', () => {
                        });
                    }
                }

                //Mimics Obsidian's Plugin class
                abstract class Plugin {
                    addRibbonIcon(icon: IconName, title: string, callback: () => any);
                }
                """,
            //Names from LucideIconService
            "dice-1",
            "dice-2",
            "dice-3",
            "dice-4",
            "dice-5",
            "dice-6",
            "dices",
            "align-horizontal-distribute-center",
            "align-vertical-distribute-center",
            //Names from other providers
            "dict");
    }

    public void testCompletionInDTsSetIcon() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        doTestCodeCompletion("main.ts", """
                class MyPlugin extends Plugin {
                    async onload() {
                        setIcon({ }, 'dic<caret>');
                    }
                }
                
                //Mimics Obsidian's API
                function setIcon(parent: object, iconId: string) {
                }
                
                abstract class Plugin {
                }
                """,
            //Names from LucideIconService
            "dice-1",
            "dice-2",
            "dice-3",
            "dice-4",
            "dice-5",
            "dice-6",
            "dices",
            "align-horizontal-distribute-center",
            "align-vertical-distribute-center",
            //Names from other providers
            "dict");
    }
}
