package com.picimako.obsidian.plugin.schema;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiFile;
import com.picimako.obsidian.ObsidianTestBase;
import com.picimako.obsidian.plugin.schema.ObsidianPluginManifestSchemaProviderFactory.ObsidianPluginManifestSchemaProvider;
import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;

/**
 * Integration test for {@link ObsidianPluginManifestSchemaProviderFactory}.
 */
public final class ObsidianPluginManifestSchemaProviderFactoryTest extends ObsidianTestBase {

    //ObsidianPluginManifestSchemaProvider

    public void testNotAvailableInNonJsonFile() {
        PsiFile psiFile = myFixture.configureByText("a_non_json_file.xml", "");

        boolean isAvailable = ObsidianPluginManifestSchemaProvider.plugin(getProject()).isAvailable(psiFile.getVirtualFile());
        assertThat(isAvailable).isFalse();
    }

    public void testNotAvailableInNonManifestJson() {
        PsiFile psiFile = myFixture.configureByText("a_non_manifest.json", "");

        boolean isAvailable = ObsidianPluginManifestSchemaProvider.plugin(getProject()).isAvailable(psiFile.getVirtualFile());
        assertThat(isAvailable).isFalse();
    }

    public void testIsAvailableInManifestJson() {
        PsiFile psiFile = myFixture.configureByText("manifest.json", "");

        boolean isAvailable = ObsidianPluginManifestSchemaProvider.plugin(getProject()).isAvailable(psiFile.getVirtualFile());
        assertThat(isAvailable).isTrue();
    }

    //ObsidianPluginManifestSchemaProviderFactory

    public void testNoProviderReturnedForNonObsidianProject() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.NONE;

        myFixture.configureByText("manifest.json", "");

        assertThat(new ObsidianPluginManifestSchemaProviderFactory().getProviders(getProject())).isEmpty();
    }

    public void testProviderIsReturnedForObsidianPluginProject() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.PLUGIN;

        myFixture.configureByText("manifest.json", "");

        var providers = new ObsidianPluginManifestSchemaProviderFactory().getProviders(getProject());
        assertThat(providers).hasSize(1);
        assertThat(providers.getFirst()).hasToString("Obsidian Plugin Manifest");
    }

    public void testProviderIsReturnedForObsidianThemeProject() {
        ObsidianProjectState.getInstance(getProject()).projectType = ObsidianProjectType.THEME;

        myFixture.configureByText("manifest.json", "");

        var providers = new ObsidianPluginManifestSchemaProviderFactory().getProviders(getProject());
        assertThat(providers).hasSize(1);
        assertThat(providers.getFirst()).hasToString("Obsidian Theme Manifest");
    }
}
