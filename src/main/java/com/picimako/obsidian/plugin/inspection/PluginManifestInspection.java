package com.picimako.obsidian.plugin.inspection;

import static com.picimako.obsidian.messages.ObsidianBundle.message;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiElementVisitor;
import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;
import org.jetbrains.annotations.NotNull;

/**
 * It reports the following issues in the {@code manifest.json} in plugin and theme projects:
 * <ul>
 *     <li>The {@code description} property must be 250 characters long maximum</li>
 *     <li>The {@code description} property must end with a dot.</li>
 *     <li>The name of the theme directory must equal to the {@code name} property.</li>
 * </ul>
 *
 * @see <a href="https://docs.obsidian.md/Plugins/Releasing/Submission+requirements+for+plugins#Keep+plugin+descriptions+short+and+simple">Keep plugin descriptions short and simple</a>
 * @see <a href="https://docs.obsidian.md/Themes/App+themes/Build+a+theme">Build a theme</a>
 */
final class PluginManifestInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (ObsidianProjectState.getInstance(holder.getProject()).projectType == ObsidianProjectType.NONE
            || !"manifest.json".equals(session.getFile().getName()))
            return PsiElementVisitor.EMPTY_VISITOR;

        return new JsonElementVisitor() {
            @Override
            public void visitProperty(@NotNull JsonProperty property) {
                String propertyName = property.getName();
                if ("description".equals(propertyName) && property.getValue() instanceof JsonStringLiteral literal) {
                    if (literal.getValue().length() > 250)
                        holder.registerProblem(literal, message("manifest.description.longer.than.250", literal.getValue().length()));

                    if (!literal.getValue().endsWith("."))
                        holder.registerProblem(literal, message("manifest.description.doesnt.end.with.dot"));
                }

                //"The name of the theme directory must exactly match the 'name' property in 'manifest.json'"
                if (ObsidianProjectState.isThemeProject(holder.getProject())
                    && "name".equals(propertyName)
                    && property.getValue() instanceof JsonStringLiteral literal
                    && !literal.getValue().equals(getProjectDirName()))
                    holder.registerProblem(literal, message("manifest.theme.name.doesnt.match.project.folder.name"), ProblemHighlightType.WARNING);
            }

            /**
             * Returns a dummy project directory name in case of testing because it is not clear if and how the project
             * directory name can be configured in tests.
             */
            private String getProjectDirName() {
                return ApplicationManager.getApplication().isUnitTestMode()
                       ? "A beautiful theme"
                       : ProjectUtil.guessProjectDir(holder.getProject()).getName();
            }
        };
    }
}
