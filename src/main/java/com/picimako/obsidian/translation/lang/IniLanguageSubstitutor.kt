package com.picimako.obsidian.translation.lang

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import com.intellij.psi.PsiManager
import ini4idea.IniLanguage

/**
 * Replaces the associated language of .txt files in the `translations` folder with Ini,
 * (without changing the file extension), so that the elements inside the translation files can be handled
 * like Ini files.
 */
internal class IniLanguageSubstitutor : LanguageSubstitutor() {
  override fun getLanguage(file: VirtualFile, project: Project): Language? {
    return if (file.parent?.name == "translations") IniLanguage.INSTANCE else null
  }
}

internal fun applyIniFileTypeOverride(project: Project) {
  ApplicationManager.getApplication().invokeLater({
    WriteAction.run<RuntimeException> {
      FileTypeManagerEx.getInstanceEx()
        .makeFileTypesChange("INI file type association has changed", EmptyRunnable.getInstance())

      PsiManager.getInstance(project).dropPsiCaches()
      DaemonCodeAnalyzer.getInstance(project).restart("INI file type override changed")
    }
  }, ModalityState.nonModal())
}