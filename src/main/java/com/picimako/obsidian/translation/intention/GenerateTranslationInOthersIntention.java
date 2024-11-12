package com.picimako.obsidian.translation.intention;

import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;
import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
import static com.picimako.obsidian.translation.OriginalLocalizationValuesCache.isProjectObsidianTranslations;
import static com.picimako.obsidian.TextRanges.endOffsetOf;
import static com.picimako.obsidian.translation.TranslationFileUtil.findPropertyByPath;
import static com.picimako.obsidian.translation.TranslationFileUtil.getPropertyPath;
import static com.picimako.obsidian.translation.TranslationFileUtil.getTopLevelObjectOf;
import static com.picimako.obsidian.translation.TranslationFilesCollector.ORIGINAL_LOCALIZATION_FILE;
import static com.picimako.obsidian.translation.TranslationFilesCollector.collectTranslationFiles;
import static com.picimako.obsidian.messages.ObsidianBundle.message;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonPsiUtil;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This intention generates/copies a selected property with its value from {@code en.json} to all other translation
 * files if that property is not yet present in them.
 * <p>
 * The intention is available only in {@code en.json}.
 */
final class GenerateTranslationInOthersIntention extends IntentionActionBase {

    /**
     * The availability check doesn't check if the selected property is already present in other languages,
     * the actual intention action invocation performs that part.
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return isProjectObsidianTranslations(project)
               && ORIGINAL_LOCALIZATION_FILE.equals(file.getName())
               && getSubjectProperty(file, editor) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        var projectRootDir = ProjectUtil.guessProjectDir(project);
        if (projectRootDir == null) return;

        //E.g. when invoked at "someProperty": "som<caret>eValue", it returns the whole JsonProperty for 'someProperty'
        var propertyToGenerate = getSubjectProperty(psiFile, editor);
        if (propertyToGenerate == null) return;

        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() ->
                    generatePropertyInOtherTranslations(project, psiFile, propertyToGenerate, projectRootDir, ProgressManager.getInstance().getProgressIndicator()),
                message("generate.property.progress.title"), true, project);
        } else {
            generatePropertyInOtherTranslations(project, psiFile, propertyToGenerate, projectRootDir, null);
        }
    }

    private void generatePropertyInOtherTranslations(@NotNull Project project, PsiFile psiFile, JsonProperty propertyToGenerate, VirtualFile projectRootDir, @Nullable("In tests.") ProgressIndicator indicator) {
        var unitTestMode = ApplicationManager.getApplication().isUnitTestMode();
        if (!unitTestMode) start(indicator, message("generate.property.collecting.context.information"));

        //Previous and next sibling properties of the property the intention is invoked on.
        // These are used to determine where to add the property in the target json relative to
        @Nullable var prevSiblingProperty = getPrevSiblingProperty(propertyToGenerate);
        @Nullable var nextSiblingProperty = getNextSiblingProperty(propertyToGenerate);

        //Get the path of the property as list of property names leading to it
        var propertyPath = getPropertyPath(propertyToGenerate, psiFile);
        var topLevelObjectInEnJson = getTopLevelObjectOf(psiFile);

        var jsonGenerator = new JsonElementGenerator(project);

        var translationFiles = collectTranslationFiles(projectRootDir, project, psiFile);
        for (int tfi = 0; tfi < translationFiles.size(); tfi++) {
            if (!unitTestMode)
                moveIndicator(indicator, translationFiles, tfi, message("generate.property.progress.step.title", translationFiles.get(tfi).getName()));

            var translationFile = translationFiles.get(tfi);
            //Traverse the properties collected, in the selected translation file, and generate each property that doesn't exist
            final var objectValue = new Ref<>(getTopLevelObjectOf(translationFile));
            var translationFileDocument = PsiDocumentManager.getInstance(project).getDocument(translationFile);

            for (int i = 0; i < propertyPath.size(); i++) {
                String name = propertyPath.get(i);
                var property = ReadAction.compute(() -> objectValue.get().findProperty(name));

                //If the property doesn't exist yet, add it to the current property object value
                if (property == null) {
                    //If we are processing the last element, the actual string property to generate
                    if (i == propertyPath.size() - 1) {
                        handleAdditionOfLeafStringProperty(
                            propertyToGenerate,
                            new GenerationContext(project, translationFile, jsonGenerator, name, objectValue, prevSiblingProperty, nextSiblingProperty, translationFileDocument, false));
                        break;
                    }

                    var propertyInEnJson = findPropertyByPath(propertyPath.stream().limit(i + 1).toList(), topLevelObjectInEnJson);
                    handleAdditionOfIntermediateProperty(
                        new GenerationContext(project, translationFile, jsonGenerator, name, objectValue,
                            getPrevSiblingProperty(propertyInEnJson),
                            getNextSiblingProperty(propertyInEnJson),
                            translationFileDocument,
                            true)
                    );
                }
                //If the property exists with an object value, proceed with traversing its object value
                else if (property.getValue() instanceof JsonObject jsonObject) {
                    objectValue.set(jsonObject);
                }
            }
        }
        if (!unitTestMode) indicator.setFraction(1.0); //progress bar and generation process completed
    }

    //Handle addition of properties

    private void handleAdditionOfIntermediateProperty(GenerationContext generationContext) {
        handleAdditionOfProperty(
            generationContext,
            //Creates a property with an empty object value
            () -> generationContext.jsonGenerator.createProperty(generationContext.name, "{}"),
            newProperty -> addPropertyAsFirstInObject(generationContext.objectValue, newProperty, generationContext.project));
    }

    /**
     * Generate the property with the string value from {@code en.json}.
     */
    private void handleAdditionOfLeafStringProperty(JsonProperty propertyToGenerate,
                                                    GenerationContext generationContext) {
        handleAdditionOfProperty(
            generationContext,
            () -> createNewProperty(generationContext.name, propertyToGenerate, generationContext.jsonGenerator),
            newProperty -> addPropertyAsFirstInObject(generationContext, newProperty));
    }

    private void handleAdditionOfProperty(GenerationContext ctx,
                                          Supplier<JsonProperty> newPropertyCreator,
                                          Consumer<JsonProperty> addPropertyAsFirstInObject) {
        if (ctx.prevSiblingProperty == null) {
            if (ctx.nextSiblingProperty == null) {
                /*
                 * If this is the only property in its parent object in en.json, add it is the only property without a closing comma.
                 *
                 * From en.json to target json
                 * { "newProperty": "value" } -> { "newProperty": "value" }
                 */
                addPropertyAsFirstInObject.accept(newPropertyCreator.get());
                return;
            }

            //If there is only a next sibling property in en.json
            var nextSiblingInTargetFile = ctx.objectValue.get().findProperty(ctx.nextSiblingProperty.getName());

            /*
             * If there is a property with the next sibling's name in the target file, add the new property before that with a closing comma.
             *
             * en.json / target json from-to
             * { "newProperty": "value", "existingProperty": "existing" }
             * { "existingProperty": "existing" } -> { "newProperty": "value", "existingProperty": "existing" }
             */
            if (nextSiblingInTargetFile != null) {
                writeCommandAction(ctx.project).run(() -> {
                    var added = (JsonProperty) ctx.objectValue.get().addBefore(newPropertyCreator.get(), nextSiblingInTargetFile);
                    ctx.addCommaInObjectAfter(added);
                    if (ctx.isObjectTraversing) ctx.proceedTraversingFrom(added.getValue());

                    replaceSpacesWithTabsInPropertyIndentation(ctx, added);
                });
                return;
            }
            /*
             * If there isn't a property with the next sibling's name in the target file, add the new property as the first property in the parent object with a closing comma.
             *
             * en.json / target json from-to
             * { "newProperty": "value", "existingProperty": "existing" }
             * { "anotherExistingProperty": "another existing" } -> { "newProperty": "value", "anotherExistingProperty": "another existing" }
             */
            addPropertyAsFirstInObject.accept(newPropertyCreator.get());
        }
        //If here is a prevSiblingProperty in en.json
        else {
            var prevSiblingInTargetFile = ctx.objectValue.get().findProperty(ctx.prevSiblingProperty.getName());
            var nextSiblingInTargetFile = getNextSiblingProperty(prevSiblingInTargetFile);


            /*
             * If there is a property with the previous sibling's name in the target file, add the new property as after that without a closing comma.
             *
             * en.json / target json from-to
             * { "existingProperty": "existing", "newProperty": "value", }
             * { "existingProperty": "existing" } -> { "existingProperty": "existing", "newProperty": "value" }
             */
            if (prevSiblingInTargetFile != null) {
                writeCommandAction(ctx.project).run(() -> {
                    //Add a comma after the previous property, if there is not already one
                    var potentialComma = prevSiblingInTargetFile.getNextSibling();
                    if (potentialComma != null && !",".equals(potentialComma.getText()))
                        ctx.addCommaInObjectAfter(prevSiblingInTargetFile);

                    //Add the new property after the comma
                    var added = (JsonProperty) ctx.objectValue.get().addAfter(newPropertyCreator.get(), prevSiblingInTargetFile.getNextSibling());
                    if (ctx.isObjectTraversing) ctx.proceedTraversingFrom(added.getValue());

                    //If there is any next sibling in the target file, add a comma as well
                    if (nextSiblingInTargetFile != null) ctx.addCommaInObjectAfter(added);

                    replaceSpacesWithTabsInPropertyIndentation(ctx, added);
                });
                return;
            }
            /*
             * If there isn't a property with the previous sibling's name in the target file, add the new property as the first property in the parent object with a closing comma.
             *
             * en.json / target json from-to
             * { "existingProperty": "existing", "newProperty": "value", }
             * { "anotherExistingProperty": "another existing" } -> { "newProperty": "value", "anotherExistingProperty": "another existing" }
             */
            addPropertyAsFirstInObject.accept(newPropertyCreator.get());
        }
    }

    //Add properties as first in a JSON object value

    /**
     * Used for adding a leaf String property.
     *
     * @param ctx      the generation context
     * @param property the JSON property to add
     */
    private void addPropertyAsFirstInObject(GenerationContext ctx, JsonProperty property) {
        writeCommandAction(ctx.project).run(() -> {
            var added = (JsonProperty) JsonPsiUtil.addProperty(ctx.objectValue.get(), property, true);
            replaceSpacesWithTabsInPropertyIndentation(ctx, added);
        });
    }

    /**
     * Used for adding an intermediate Object property.
     * It also saves the added properties object value in {@code object} in order to be able to continue traversing down.
     *
     * @param object   the JSON object holder to add the property into
     * @param property the JSON property to add
     */
    private void addPropertyAsFirstInObject(Ref<JsonObject> object, JsonProperty property, Project project) {
        var added = runWriteCommandAction(project, (Computable<JsonProperty>) () -> (JsonProperty) JsonPsiUtil.addProperty(object.get(), property, true));
        object.set((JsonObject) added.getValue());
    }

    //Create property

    /**
     * Creates a new JSON property with the given {@code name} and its value set as the value of {@code propertyToGenerate}.
     */
    private JsonProperty createNewProperty(String name, JsonProperty propertyToGenerate, JsonElementGenerator generator) {
        return generator.createProperty(name, propertyToGenerate.getValue().getText());
    }

    //Formatting

    /**
     * Reformats the newly {@code added} property to use tabs as indents.
     */
    private void replaceSpacesWithTabsInPropertyIndentation(GenerationContext ctx, JsonProperty added) {
        if (!ctx.isObjectTraversing) {
            /*
              Reformat the added property. This will align the property properly, applying indents based on the IDE's JSON code style settings, from:
              {
                "prevProperty": "value","newProperty": "newValue"
              }
              to
              {
                "prevProperty": "value",
                "newProperty": "newValue"
              }
             */
            CodeStyleManager.getInstance(ctx.project).reformatRange(ctx.translationFile, added.getTextOffset(), endOffsetOf(added));

            /*
              This replaces all 4-whitespaces or 2-whitespaces to tab character as the translation files use tab as indent.
              I could not find how to get the indent value from the JSON code style settings (and having project and application level settings just complicates things),
               so it uses a 4 to 2 whitespaces fallback mechanism.
             */
            for (var property : ctx.objectValue.get().getPropertyList()) {
                var indentRangeOfProperty = getLineIndentRange(ctx.document, ctx.document.getLineNumber(property.getTextOffset()));
                String indentation = ctx.document.getText(indentRangeOfProperty);

                if (indentation.contains("    ")) {
                    indentation = indentation.replaceAll(" {4}", "\t");
                    ctx.document.replaceString(indentRangeOfProperty.getStartOffset(), indentRangeOfProperty.getEndOffset(), indentation);
                } else if (indentation.contains("  ")) {
                    indentation = indentation.replaceAll(" {2}", "\t");
                    ctx.document.replaceString(indentRangeOfProperty.getStartOffset(), indentRangeOfProperty.getEndOffset(), indentation);
                }
            }
        }
    }

    private TextRange getLineIndentRange(Document document, int line) {
        var lineStart = document.getLineStartOffset(line);
        var indentEnd = StringUtil.skipWhitespaceForward(document.getCharsSequence(), lineStart);
        return TextRange.create(lineStart, indentEnd);
    }

    //Intention action configuration

    @Override
    public @IntentionName @NotNull String getText() {
        return message("generate.property.into.other.translations");
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return message("generate.property.into.other.translations");
    }

    /**
     * Wrapper for many contextual data, so that they can be passed around easier.
     */
    private record GenerationContext(
        Project project,
        PsiFile translationFile,
        JsonElementGenerator jsonGenerator,
        String name,
        Ref<JsonObject> objectValue,
        @Nullable JsonProperty prevSiblingProperty,
        @Nullable JsonProperty nextSiblingProperty,
        Document document,
        boolean isObjectTraversing
    ) {
        void addCommaInObjectAfter(JsonProperty added) {
            objectValue.get().addAfter(jsonGenerator.createComma(), added);
        }

        void proceedTraversingFrom(JsonValue value) {
            objectValue.set((JsonObject) value);
        }
    }
}
