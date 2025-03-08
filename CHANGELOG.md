<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Obsidian Ecosystem Changelog

## [Unreleased]

## [0.2.0]
### Changed
- Updated supported IDE range to 2024.3 - 2025.1.

## [0.1.0]
### Added
- **Translations**: Added intention action to be able to navigate to other translations of selected properties.
- **Translations**: Added intention action that generates a property selected in `en.json` to all other translation files,
  if that property is not yet present in them.
- **Translations**: Added intention action that deletes a property selected in `en.json` in every translation files.
- **Translations**: Added an annotator that makes `{{...}}` variables stand out from the translation values.
- **Translations**: Added an inspection that can report missing and invalid `{{...}}` variables.
- **Translations**: Added an inspection that can report missing keys in non-en.json translation files.
- **Plugin / Theme**: Added JSON schema for the plugin and theme `manifest.json`.
- **Plugin / Theme**: Added an inspection that validates the properties in the `manifest.json`.
- **Plugin / Theme**: Added code completion that provides the list of available lucide.dev icon names in `setIcon()` and `addRibbonIcon()` function calls.
- **Plugin / Theme**: Added an inspection that reports `Plugin.addCommand()` calls in which the command id is prefixed with the plugin id.
