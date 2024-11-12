package com.picimako.obsidian.plugin.schema;

import static com.picimako.obsidian.messages.ObsidianBundle.messagePointer;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.extension.SchemaType;
import com.picimako.obsidian.settings.ObsidianProjectState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Provides JSON schemas for Obsidian plugin and theme plugin {@code manifest.json} files.
 * <p>
 * The distinction is made between the two types of plugins because the
 * <a href="https://docs.obsidian.md/Reference/Manifest">Manifest</a> documentation describes
 * different property availability depending on the plugin type.
 */
final class ObsidianPluginManifestSchemaProviderFactory implements JsonSchemaProviderFactory, DumbAware {

    @Override
    public @NotNull List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        if (ObsidianProjectState.isThemeProject(project))
            return List.of(ObsidianPluginManifestSchemaProvider.themePlugin(project));
        if (ObsidianProjectState.isPluginProject(project))
            return List.of(ObsidianPluginManifestSchemaProvider.plugin(project));

        return List.of();
    }

    static class ObsidianPluginManifestSchemaProvider implements JsonSchemaFileProvider {
        //Paths are relative to src/main/resources
        private static final @NonNls String PLUGIN = "/schema/plugin-manifest.yaml";
        private static final @NonNls String THEME = "/schema/theme-manifest.yaml";

        @NotNull
        private final Project project;
        @NotNull
        private final String schemaFile;
        private final Supplier<String> schemaName;

        public ObsidianPluginManifestSchemaProvider(@NotNull Project project, @NotNull String schemaFile, Supplier<String> schemaName) {
            this.project = project;
            this.schemaFile = schemaFile;
            this.schemaName = schemaName;
        }

        @Override
        public boolean isAvailable(@NotNull VirtualFile file) {
            return file.exists()
                   && file.isValid()
                   && file.getFileType().equals(JsonFileType.INSTANCE)
                   && "manifest.json".equals(file.getName());
        }

        //User visible when the manifest.json is opened in the currently selected editor
        @Override
        public boolean isUserVisible() {
            var selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
            if (selectedEditor == null) return false;

            var editedFile = selectedEditor.getFile();
            return editedFile != null && isAvailable(editedFile);
        }

        @Override
        public @NotNull @Nls String getName() {
            return schemaName.get();
        }

        @Override
        public @Nullable VirtualFile getSchemaFile() {
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                try {
                    return getMappingSchemaFile();
                } catch (AssertionError e) {
                    return null;
                }
            } else return getMappingSchemaFile();
        }

        @Nullable
        protected VirtualFile getMappingSchemaFile() {
            return JsonSchemaProviderFactory.getResourceFile(ObsidianPluginManifestSchemaProvider.class, schemaFile);
        }

        @NotNull
        @Override
        public SchemaType getSchemaType() {
            return SchemaType.schema;
        }

        @VisibleForTesting
        static ObsidianPluginManifestSchemaProvider plugin(Project project) {
            return new ObsidianPluginManifestSchemaProvider(project, PLUGIN, messagePointer("plugin.manifest.schema.name"));
        }

        @VisibleForTesting
        static ObsidianPluginManifestSchemaProvider themePlugin(Project project) {
            return new ObsidianPluginManifestSchemaProvider(project, THEME, messagePointer("theme.manifest.schema.name"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObsidianPluginManifestSchemaProvider that = (ObsidianPluginManifestSchemaProvider) o;
            return Objects.equals(project, that.project) && Objects.equals(schemaName, that.schemaName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(project, schemaName);
        }

        @Override
        public String toString() {
            return schemaName.get();
        }
    }
}
