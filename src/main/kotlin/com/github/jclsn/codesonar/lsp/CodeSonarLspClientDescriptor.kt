package com.github.jclsn.codesonar.lsp

import com.github.jclsn.codesonar.settings.CodeSonarSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClientDescriptor
import java.nio.file.Path

/** Starts codesonar-lsp for the C and C++ files of a workspace. */
class CodeSonarLspClientDescriptor(
    project: Project,
    private val root: VirtualFile,
    private val executable: Path,
) : LspClientDescriptor(project, "CodeSonar", root) {
    override fun isSupportedFile(file: VirtualFile): Boolean =
        SourceFiles.isSource(file.name) && VfsUtilCore.isAncestor(root, file, false)

    override fun createCommandLine(): GeneralCommandLine =
        GeneralCommandLine(executable.toString()).withWorkDirectory(root.path)

    override fun createInitializationOptions(): Any =
        CodeSonarSettings.getInstance().initializationOptions()
}
