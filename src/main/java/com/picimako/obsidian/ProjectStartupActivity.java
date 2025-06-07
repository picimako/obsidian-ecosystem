package com.picimako.obsidian;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCloseListener;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiManager;
import com.picimako.obsidian.plugin.ManifestDataCache;
import com.picimako.obsidian.plugin.ManifestJsonListener;
import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;
import com.picimako.obsidian.translation.OriginalLocalizationValuesCache;
import com.picimako.obsidian.translation.OriginalLocalizationValuesListener;
import com.picimako.obsidian.translation.TranslationReader;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;

/**
 * Executes stuff after a project is initialized.
 */
@SuppressWarnings("UnstableApiUsage")
final class ProjectStartupActivity implements ProjectActivity {
    private static final Key<Disposable> IS_ORIGINAL_VALUES_LISTENER_REGISTERED = Key.create("isOriginalValueListenerRegistered");
    private static final Key<Disposable> IS_MANIFEST_JSON_LISTENER_REGISTERED = Key.create("isManifestJsonListenerRegistered");

    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (project.getName().contains("obsidian-translations")) {
            OriginalLocalizationValuesCache.getInstance(project).setProjectObsidianTranslations(true);
            TranslationReader.readOriginal(project);
            registerOriginalValuesListener(project);
        } else if (ObsidianProjectState.isPluginProject(project)) {
            registerManifestJsonListener(project);
            //NOTE: Currently this is registered only for plugin projects, because it caches only the 'id' property for now
            ManifestDataCache.getInstance(project).cacheProperties();
        }

        return Unit.INSTANCE;
    }

    private void registerOriginalValuesListener(Project project) {
        var isListenerRegistered = Disposer.newDisposable("IS_ORIGINAL_VALUES_LISTENER_REGISTERED");

        PsiManager.getInstance(project).addPsiTreeChangeListener(new OriginalLocalizationValuesListener(), isListenerRegistered);
        project.putUserData(IS_ORIGINAL_VALUES_LISTENER_REGISTERED, isListenerRegistered);

        //Make sure that the listener is disposed upon project close
        project.getMessageBus().connect().subscribe(ProjectCloseListener.TOPIC, new ProjectCloseListener() {
            @Override
            public void projectClosing(@NotNull Project project) {
                if (project.getUserData(IS_ORIGINAL_VALUES_LISTENER_REGISTERED) != null) {
                    project.putUserData(IS_ORIGINAL_VALUES_LISTENER_REGISTERED, null);
                    Disposer.dispose(isListenerRegistered);
                }
            }
        });
    }

    private void registerManifestJsonListener(Project project) {
        var isListenerRegistered = Disposer.newDisposable("IS_MANIFEST_JSON_LISTENER_REGISTERED");

        PsiManager.getInstance(project).addPsiTreeChangeListener(new ManifestJsonListener(), isListenerRegistered);
        project.putUserData(IS_MANIFEST_JSON_LISTENER_REGISTERED, isListenerRegistered);

        //Make sure that the listener is disposed upon project close
        project.getMessageBus().connect().subscribe(ProjectCloseListener.TOPIC, new ProjectCloseListener() {
            @Override
            public void projectClosing(@NotNull Project project) {
                if (project.getUserData(IS_MANIFEST_JSON_LISTENER_REGISTERED) != null) {
                    project.putUserData(IS_MANIFEST_JSON_LISTENER_REGISTERED, null);
                    Disposer.dispose(isListenerRegistered);
                }
            }
        });
    }
}
