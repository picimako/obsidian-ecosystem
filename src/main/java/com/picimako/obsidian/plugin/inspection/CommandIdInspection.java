package com.picimako.obsidian.plugin.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.TypeScriptFileType;
import com.intellij.lang.javascript.TypeScriptJSXFileType;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.psi.PsiElementVisitor;
import com.picimako.obsidian.plugin.ManifestDataCache;
import com.picimako.obsidian.settings.ObsidianProjectState;
import com.picimako.obsidian.settings.ObsidianProjectType;
import com.picimako.obsidian.messages.ObsidianBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Reports issues with command ids specified inside the {@code Plugin.addCommand({ id: '...'})} property:
 * <ul>
 *     <li>if the command id is prefixed with the plugin id specified in the {@code manifest.json}</li>
 * </ul>
 *
 * @see <a href="https://docs.obsidian.md/Plugins/Releasing/Submission+requirements+for+plugins#Don't%20include%20the%20plugin%20ID%20in%20the%20command%20ID">Don't include the plugin ID in the command ID</a>
 */
final class CommandIdInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (!ObsidianProjectState.isPluginProject(holder.getProject())) return PsiElementVisitor.EMPTY_VISITOR;

        var fileType = session.getFile().getFileType();
        if (fileType != TypeScriptFileType.INSTANCE && fileType != TypeScriptJSXFileType.INSTANCE)
            return PsiElementVisitor.EMPTY_VISITOR;

        return new JSElementVisitor() {
            @Override
            public void visitJSCallExpression(@NotNull JSCallExpression jsCall) {
                if (
                    //An 'addCommand()' function call is being observed
                    jsCall.getMethodExpression() instanceof JSReferenceExpression ref
                    && "addCommand".equals(ref.getReferenceName())
                    //The call has at least 1 non-null object literal argument with a string 'id' attribute
                    && jsCall.getArguments().length > 0
                    && jsCall.getArguments()[0] instanceof JSObjectLiteralExpression object) {

                    var idProperty = object.findProperty("id");
                    if (idProperty != null
                        && idProperty.getValue() instanceof JSLiteralExpression literal
                        && literal.getStringValue() != null
                        //The 'addCommand()' call is the one defined in Obsidian's Plugin class
                        && ref.resolve() instanceof TypeScriptFunction function
                        && function.getParent() instanceof TypeScriptClass tsClass
                        && "Plugin".equals(tsClass.getName())) {

                        //The command id is prefixed with the plugin id specified in manifest.json
                        String id = ManifestDataCache.getInstance(holder.getProject()).getManifest().getId();
                        if (id != null && literal.getStringValue().startsWith(id))
                            holder.registerProblem(literal, ObsidianBundle.message("plugin.add.command.id.prefixed.with.plugin.id", id));
                    }
                }
            }
        };
    }
}