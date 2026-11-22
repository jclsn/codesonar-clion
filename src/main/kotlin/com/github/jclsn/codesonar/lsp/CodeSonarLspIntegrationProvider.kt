package com.github.jclsn.codesonar.lsp

import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.settings.CodeSonarSettings
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Starts the server with the first C or C++ file of a workspace: the
 * closest directory above the file with a `codesonar.conf`, a `.codesonar`
 * or a `.git`, else the project directory.
 */
class CodeSonarLspIntegrationProvider : LspIntegrationProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter,
    ) {
        if (!file.isInLocalFileSystem || !SourceFiles.isSource(file.name)) return

        val root = workspaceRoot(project, file) ?: return
        val configured = CodeSonarSettings.getInstance().state.serverPath
        val executable = ServerExecutable.find(configured) { PathEnvironmentVariableUtil.findFirst(it) }

        if (executable == null) {
            reportMissing(project, configured)
            return
        }

        clientStarter.ensureClientStarted(CodeSonarLspClientDescriptor(project, root, executable))
    }

    private fun workspaceRoot(project: Project, file: VirtualFile): VirtualFile? {
        val found = WorkspaceRoot.find(Path.of(file.path))
            ?.let { LocalFileSystem.getInstance().findFileByNioFile(it) }
        if (found != null) return found

        return project.guessProjectDir()?.takeIf { VfsUtilCore.isAncestor(it, file, false) }
    }

    /** Says once per project and path that the server was not found. */
    private fun reportMissing(project: Project, configured: String) {
        if (reported.add(project.locationHash + configured)) {
            Notifier.error(
                project,
                "The server \"$configured\" was not found. Install codesonar-lsp with " +
                    "\"cargo install --path .\" or set its path in Settings | Tools | CodeSonar.",
            )
        }
    }

    private companion object {
        val reported: MutableSet<String> = ConcurrentHashMap.newKeySet()
    }
}
