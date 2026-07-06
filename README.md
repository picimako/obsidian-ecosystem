# Obsidian Ecosystem - Plugin for JetBrains IDEs

<!-- Plugin description -->
This JetBrains IDE plugin provides various features for those that work with [Obsidian.md](https://obsidian.md)'s
translations.

The plugin is not released on the JetBrains Marketplace or on any other outlet. Its main purpose is to demonstrate
that such small aspects of an ecosystem like translations can have meaningful IDE integrations, even if it is for a
small number of contributors. However, the plugin is functional, so feel free to use it for actual Obsidian translation
activities. You can download the plugin archive from the [download](download/obsidian-ecosystem-0.1.0.zip) directory.
<!-- Plugin description end -->

----

<!-- TOC -->
* [Show translation of property in another language](#show-translation-of-property-in-another-language)
* [Variable annotations](#variable-annotations)
* [Variable usage inspections](#variable-usage-inspections)
* [Untranslated entries](#untranslated-entries)
<!-- TOC -->

The features described in this document are all created for the [obsidian-translations](https://github.com/obsidianmd/obsidian-translations/) project.

### Show translation of property in another language

This intention action lets users navigate to the translation of the selected property in other languages.

It presents the user the list of available translation files, and if the property is available in the file selected, the IDE opens the file
and navigates to that property.

The language names displayed inside parenthesis are their localized versions.

![List of available translation files](assets/list_of_available_translation_files.png)

### Variable annotations

In order to make `{{...}}` variables stand out in translation strings, this annotator applies styling to them.

![variable annotations](assets/variable_annotations.PNG)

### Variable usage inspections

This inspection reports the following issues with `{{...}}` variables:
- **missing variable**: one or more variables present in the English value, are not used in a translation
- **invalid variable**: one or more variables are specified with names that are not present in the English value

![variable reports](assets/variable_reports.png)

### Untranslated entries

This inspection reports localization properties that are untranslated, i.e. have the same value as their original,
English counterparts. This can aid translators to identify entries that are not yet translated.

![untranslated_items](assets/untranslated_items.png)

In the inspections settings there is a customizable list of section names to ignore entries that don't need translation,
like abbreviations, or are not allowed to be translated, like brand names.

The feature comes with a default set of such ignored entries but one can remove or add any for language specific configuration.

![untranslated_items_ignored](assets/untranslated_items_ignored.png)
