package com.picimako.obsidian.inlay;

import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.TranslationReader;

public class OriginalValueInlayHintsProviderTest extends DeclarativeInlayHintsProviderTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/inlay/originalvalues";
    }

    public void testOriginalValuesInlayHints() {
        OriginalLocalizationValuesCache.getInstance(getProject()).setProjectObsidianTranslations(true);
        myFixture.copyFileToProject("en.json");
        TranslationReader.readOriginal(getProject());

        doTestProvider("hu.json",
            //language=JSON
            """
                {
                	"setting": {
                		/*<# block [Options] #>*/
                		"options": "Beállítások",
                		/*<# block [Plugin] #>*/
                		"plugin": "Bővítmény",
                		/*<# block [Core plugins] #>*/
                		"builtin-plugins": "Alap bővítmények",
                		/*<# block [Plugin options] #>*/
                		"plugin-options": "Bővítmény beállítások",
                		/*<# block [Example: folder 1/folder 2] #>*/
                		"folder-path-example-placeholder": "Példa: mappa 1/mappa 2",
                		/*<# block [Example: folder/note] #>*/
                		"file-path-example-placeholder": "Példa: mappa/jegyzet",
                		/*<# block [Changing this option requires a restart to take effect.] #>*/
                		"msg-restart-required": "Ezen beállítás érvényesítéséhez újraindítás szükséges.",
                		"appearance": {
                		  /*<# block ['' Currently applied font: ''] #>*/
                		  "label-single-font-currently-in-effect": " Jelenleg alkalmazott betűtípus: "
                		}
                    }
                }
                """, new OriginalValueInlayHintsProvider());
    }
}