package com.github.jclsn.codesonar

import com.github.jclsn.codesonar.model.AnalyzeCommand
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path

/**
 * Runs `codesonar analyze` for the workspace in the Run tool window and
 * shows the new analysis when it succeeds.
 */
@Service(Service.Level.PROJECT)
class AnalysisRunner(private val project: Project) {
    @Volatile
    private var handler: ProcessHandler? = null

    val isAnalyzing: Boolean
        get() = handler?.isProcessTerminated == false

    /** Starts an analysis, which [name] names on the hub. */
    fun analyze(name: String? = null) {
        if (isAnalyzing) {
            Notifier.error(project, "An analysis is already running")
            return
        }

        val argument = name?.trim()?.takeIf { it.isNotEmpty() }?.let { mapOf("name" to it) }

        CodeSonarService.getInstance(project).execute(Commands.ANALYZE_COMMAND, argument) { result ->
            val command = AnalyzeCommand.parse(result)

            if (command == null) {
                Notifier.error(project, "The server sent no command for the analysis")
            } else if (!isAnalyzing) {
                start(command)
            }
        }
    }

    private fun start(command: AnalyzeCommand) {
        val commandLine = GeneralCommandLine(command.command)
            .withWorkDirectory(command.cwd)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        val process = try {
            ColoredProcessHandler(commandLine)
        } catch (error: ExecutionException) {
            deleteFiles(command.temporaryFiles)
            Notifier.error(project, error.message ?: "The analysis could not be started")
            return
        }
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console

        console.attachToProcess(process)
        ProcessTerminatedListener.attach(process)
        process.addProcessListener(object : ProcessListener {
            override fun processTerminated(event: ProcessEvent) {
                analyzed(event.exitCode, command.temporaryFiles)
            }
        })
        handler = process

        val descriptor = RunContentDescriptor(console, process, console.component, "CodeSonar Analyze")
        RunContentManager.getInstance(project)
            .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)
        process.startNotify()
    }

    private fun analyzed(code: Int, temporaryFiles: List<String>) {
        deleteFiles(temporaryFiles)

        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater

            if (code != 0) {
                Notifier.error(project, "Analysis failed with exit code $code")
            } else {
                CodeSonarService.getInstance(project).execute(Commands.LOAD, mapOf("latest" to true)) {}
            }
        }
    }

    private fun deleteFiles(files: List<String>) {
        for (file in files) {
            try {
                Files.deleteIfExists(Path.of(file))
            } catch (_: Exception) {
                // Nothing to do about a file that cannot be deleted.
            }
        }
    }

    companion object {
        fun getInstance(project: Project): AnalysisRunner = project.service()
    }
}
