package com.github.jclsn.codesonar

import com.github.jclsn.codesonar.lsp.CodeSonarLspIntegrationProvider
import com.github.jclsn.codesonar.model.Json
import com.github.jclsn.codesonar.model.Status
import com.github.jclsn.codesonar.model.string
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import java.nio.file.Path

/**
 * Runs the commands of the server and keeps the status it sent last. All
 * CodeSonar logic lives in the server.
 */
@Service(Service.Level.PROJECT)
class CodeSonarService(private val project: Project, private val scope: CoroutineScope) {
    /** The status of the shown analysis, as the server sent it last. */
    @Volatile
    var status: Status = Status()
        private set

    /** What is loading, or null. */
    @Volatile
    var loading: String? = null
        private set

    /** The running servers of the project. */
    fun clients(): List<LspClient> = LspClientManager.getInstance(project)
        .getClients(CodeSonarLspIntegrationProvider::class.java)
        .filter { it.state == LspServerState.Running }

    /** The running server of the file in the editor, else any running one. */
    fun client(): LspClient? {
        val clients = clients()
        val file = FileEditorManager.getInstance(project).selectedEditor?.file

        return clients.firstOrNull { file != null && it.owns(file) } ?: clients.firstOrNull()
    }

    private fun LspClient.owns(file: VirtualFile): Boolean =
        descriptor.roots.any { VfsUtilCore.isAncestor(it, file, false) }

    /**
     * Runs a command of the server and calls [done] with its result in the
     * UI thread. Errors are shown; [failed] is called after them.
     */
    fun execute(
        command: String,
        argument: Any? = null,
        failed: () -> Unit = {},
        done: (JsonElement) -> Unit,
    ) {
        val client = client()

        if (client == null) {
            Notifier.error(project, NOT_RUNNING)
            failed()
            return
        }

        val params = ExecuteCommandParams(command, listOfNotNull(argument))

        scope.launch {
            // Errors are results here, however the platform treats failed requests.
            val outcome = try {
                client.sendRequest { server ->
                    server.workspaceService.executeCommand(params)
                        .handle { result, error -> Outcome(result, error) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Outcome(null, error)
            }

            withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
                if (project.isDisposed) return@withContext

                if (outcome == null || outcome.error != null) {
                    Notifier.error(project, errorMessage(command, outcome?.error))
                    failed()
                } else {
                    done(Json.tree(outcome.result))
                }
            }
        }
    }

    private class Outcome(val result: Any?, val error: Throwable?)

    /** The server sent `codesonar/loading`. */
    fun onLoading(params: JsonObject) {
        val message = loadingMessage(params, project.basePath) ?: return

        loading = message
        showInfo(message)
        project.messageBus.syncPublisher(CodeSonarListener.TOPIC).loading(message)
    }

    /** The server sent `codesonar/loaded`. */
    fun onLoaded(params: JsonObject) {
        val loaded = Status.parse(params)

        status = loaded
        loading = null
        showInfo("CodeSonar: ${loaded.summary()}")
        project.messageBus.syncPublisher(CodeSonarListener.TOPIC).loaded(loaded)
    }

    /** Shows a message in the status bar, like other finished actions. */
    private fun showInfo(text: String) {
        scope.launch(Dispatchers.EDT + ModalityState.any().asContextElement()) {
            if (!project.isDisposed) {
                WindowManager.getInstance().getStatusBar(project)?.info = text
            }
        }
    }

    /** Opens a file that the server may just have written. */
    fun openFile(path: String, line: Int = -1, column: Int = 0) {
        scope.launch(Dispatchers.IO) {
            val file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Path.of(path))

            withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
                if (project.isDisposed) return@withContext

                if (file == null) {
                    Notifier.error(project, "Cannot open $path")
                } else if (line >= 0) {
                    OpenFileDescriptor(project, file, line, column).navigate(true)
                } else {
                    OpenFileDescriptor(project, file).navigate(true)
                }
            }
        }
    }

    companion object {
        const val NOT_RUNNING = "The server is not running: open a file of the project first"

        fun getInstance(project: Project): CodeSonarService = project.service()

        /** What `codesonar/loading` announces, with paths relative to [basePath]. */
        fun loadingMessage(params: JsonObject, basePath: String?): String? {
            val analysis = params.string("analysis")
            val file = params.string("file")
            val project = params.string("project")

            return when {
                analysis != null -> "CodeSonar: Loading analysis $analysis..."
                file != null -> "CodeSonar: Loading ${relative(file, basePath)}..."
                project != null -> "CodeSonar: Loading the newest analysis of $project..."
                else -> null
            }
        }

        private fun relative(path: String, basePath: String?): String =
            if (basePath != null && path.startsWith("$basePath/")) path.substring(basePath.length + 1) else path

        /** The message of a failed command, which the server explains. */
        fun errorMessage(command: String, error: Throwable?): String {
            val response = generateSequence(error) { it.cause }
                .filterIsInstance<ResponseErrorException>()
                .firstOrNull()
            val message = response?.responseError?.message ?: error?.message

            return message?.takeIf { it.isNotBlank() } ?: "$command failed"
        }
    }
}
