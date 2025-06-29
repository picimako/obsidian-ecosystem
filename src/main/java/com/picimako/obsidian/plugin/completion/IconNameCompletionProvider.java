package com.picimako.obsidian.plugin.completion;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.TypeScriptFileType;
import com.intellij.lang.javascript.TypeScriptJSXFileType;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiFilePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.picimako.obsidian.settings.ObsidianProjectState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Provides code completion of lucide.dev icon names inside {@code setIcon()} and {@code addRibbonIcon()} function calls.
 * <p>
 * Based on <a href="https://docs.obsidian.md/Plugins/User+interface/Icons">Icons</a> the names are provided only up to
 * lucide v0.292.0.
 */
final class IconNameCompletionProvider extends CompletionContributor {
    private static final Set<String> SET_ICON_CLASS_NAMES = Set.of("ButtonComponent", "ExtraButtonComponent", "Menu", "MenuItem");
    private static final PsiFilePattern.@NotNull Capture<PsiFile> TS_OR_TSX_FILE = psiFile().with(patternCondition("TS or TSX file",
        psiFile -> {
            var fileType = psiFile.getFileType();
            return fileType == TypeScriptFileType.INSTANCE || fileType == TypeScriptJSXFileType.INSTANCE;
        }));

    /**
     * Supported calls:
     * <ul>
     *     <li>{@code setIcon(IconName)} in {@code ButtonComponent}, {@code ExtraButtonComponent}, {@code Menu}, {@code MenuItem}</li>
     *     <li>{@code addRibbonIcon(IconName)} in {@code Plugin}</li>
     * </ul>
     */
    private static final PsiElementPattern.Capture<PsiElement> ADD_RIBBON_ICON_SET_ICON_FIRST_ARGUMENT_PATTERN =
        psiElement(JSTokenTypes.STRING_LITERAL)
            .with(patternCondition("first argument of its parent function call", element -> isNthArgumentOfCall(element, 0)))
            .withSuperParent(2, psiElement(JSArgumentList.class)
                .withParent(psiElement(JSCallExpression.class)
                    .with(patternCondition("Plugin.addRibbonIcon() or setIcon() function call",
                        jsCall ->
                            isEligibleFunctionCall(jsCall, (ref, function) -> {
                                if (function.getParent() instanceof TypeScriptClass tsClass) {
                                    String referenceName = ref.getReferenceName();
                                    String className = tsClass.getName();
                                    return ("addRibbonIcon".equals(referenceName) && "Plugin".equals(className))
                                           || ("setIcon".equals(referenceName) && SET_ICON_CLASS_NAMES.contains(className));
                                }
                                return false;
                            })))
                    .inFile(TS_OR_TSX_FILE)));

    /**
     * Supported call: {@code setIcon(..., IconName)} in {@code obsidian.d.ts}.
     */
    private static final PsiElementPattern.Capture<PsiElement> PLUGIN_SET_ICON_SECOND_ARGUMENT_PATTERN =
        psiElement(JSTokenTypes.STRING_LITERAL)
            .with(patternCondition("second argument of its parent function call", element -> isNthArgumentOfCall(element, 1)))
            .withSuperParent(2, psiElement(JSArgumentList.class)
                .withParent(psiElement(JSCallExpression.class)
                    .with(patternCondition("Plugin.setIcon(..., '') function call",
                        jsCall -> isEligibleFunctionCall(jsCall, (ref, function) -> {
                                return "setIcon".equals(ref.getReferenceName())
                                       && function.getParent() instanceof PsiFile file
                                       && (ApplicationManager.getApplication().isUnitTestMode() || "obsidian.d.ts".equals(file.getName()));
                            }
                        )))
                    .inFile(TS_OR_TSX_FILE)));

    private static final CompletionProvider<CompletionParameters> ICON_NAME_COMPLETION_PROVIDER =
        new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                LucideIconService.getInstance().getIconNames().stream()
                    .map(iconName -> LookupElementBuilder.create(iconName)
                        //This icon is the one that looks closest to lucide's own icon, without having to create a custom icon for that
                        .withIcon(AllIcons.Actions.ProjectWideAnalysisOff)
                        .withCaseSensitivity(true))
                    .forEach(result::addElement);

                result.stopHere();
            }
        };

    public IconNameCompletionProvider() {
        extend(CompletionType.BASIC, ADD_RIBBON_ICON_SET_ICON_FIRST_ARGUMENT_PATTERN, ICON_NAME_COMPLETION_PROVIDER);
        extend(CompletionType.BASIC, PLUGIN_SET_ICON_SECOND_ARGUMENT_PATTERN, ICON_NAME_COMPLETION_PROVIDER);
    }

    //Helpers

    /**
     * Returns if {@code element} is a String literal as the {@code n}th argument of its parent function call.
     *
     * @param element the potential String literal argument
     * @param n       the 0-based argument index
     */
    private static boolean isNthArgumentOfCall(PsiElement element, int n) {
        if (ObsidianProjectState.isPluginProject(element.getProject())
            && element.getParent() instanceof JSLiteralExpression literal
            && literal.getParent() instanceof JSArgumentList argumentList) {
            var arguments = argumentList.getArguments();
            return arguments.length > n && arguments[n].isEquivalentTo(literal);
        }
        return false;
    }

    /**
     * Returns if {@code jsCallExpression} is an eligible function call to provide code completion based on the criteria supplied by {@code isEligibleFunction}.
     */
    private static boolean isEligibleFunctionCall(JSCallExpression jsCallExpression, BiPredicate<JSReferenceExpression, TypeScriptFunction> isEligibleFunction) {
        return jsCallExpression.getMethodExpression() instanceof JSReferenceExpression ref
               && ref.resolve() instanceof TypeScriptFunction function
               && isEligibleFunction.test(ref, function);
    }

    private static <T> PatternCondition<T> patternCondition(String debugMethodName, Predicate<T> accepts) {
        return new PatternCondition<>(debugMethodName) {
            @Override
            public boolean accepts(@NotNull T element, ProcessingContext context) {
                return accepts.test(element);
            }
        };
    }
}
