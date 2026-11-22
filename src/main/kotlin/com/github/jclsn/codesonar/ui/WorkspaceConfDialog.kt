package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.WorkspaceProject
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Asks for the basic settings that start the workspace configuration: the
 * hub address, whether later analyses are incremental, the project name on
 * the hub and the build commands.
 */
class WorkspaceConfDialog(project: Project, private val workspace: WorkspaceProject) : DialogWrapper(project) {
    private val suggested = workspace.suggested
    private val hubAddress = JBTextField(suggested.hubAddress, 36).apply { emptyText.text = "host:port" }
    private val incremental = JBCheckBox("Incremental build", suggested.incremental)
    private val name = JBTextField(suggested.project, 36).apply { emptyText.text = "name" }
    private val build = JBTextField(suggested.build, 36).apply { emptyText.text = "make -B" }
    private val incrementalBuild = JBTextField(suggested.incrementalBuild, 36).apply { emptyText.text = "make" }

    init {
        title = "CodeSonar Workspace Configuration"
        setOKButtonText("Save")
        incremental.addActionListener { incrementalBuild.isEnabled = incremental.isSelected }
        incrementalBuild.isEnabled = incremental.isSelected
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row { label(missing(workspace)) }
        row {
            comment(
                "A basic configuration must be defined to run analyses.<br>" +
                    "The first analysis needs a full rebuild, later ones only what changed.",
            )
        }
        row("Hub address:") { cell(hubAddress).align(AlignX.FILL) }
        row("") { cell(incremental) }
        row("Project name:") { cell(name).align(AlignX.FILL) }
        row("Full build command:") { cell(build).align(AlignX.FILL) }
        row("Incremental build command:") { cell(incrementalBuild).align(AlignX.FILL) }
    }

    override fun getPreferredFocusedComponent(): JComponent = hubAddress

    public override fun doValidate(): ValidationInfo? {
        val field = when (invalid(name.text, build.text, incremental.isSelected, incrementalBuild.text)) {
            null -> return null
            Field.NAME -> name
            Field.BUILD -> build
            Field.INCREMENTAL_BUILD -> incrementalBuild
        }
        val message = if (field === incrementalBuild) {
            "An incremental build needs an incremental build command"
        } else {
            "The configuration needs a project name and a full build command"
        }

        return ValidationInfo(message, field)
    }

    /** The argument of `codesonar.initConf`. */
    fun argument(): Map<String, Any> =
        argument(hubAddress.text, incremental.isSelected, name.text, build.text, incrementalBuild.text)

    enum class Field { NAME, BUILD, INCREMENTAL_BUILD }

    companion object {
        /** Says which file or section of it is missing. */
        fun missing(workspace: WorkspaceProject): String {
            val file = workspace.workspaceConf.substringAfterLast('/')

            return if (workspace.workspaceConfExists) {
                "No configuration block for codesonar-lsp in $file."
            } else {
                "No $file found."
            }
        }

        /** The first field that lacks a value, or null. */
        fun invalid(name: String, build: String, incremental: Boolean, incrementalBuild: String): Field? = when {
            name.isBlank() -> Field.NAME
            build.isBlank() -> Field.BUILD
            incremental && incrementalBuild.isBlank() -> Field.INCREMENTAL_BUILD
            else -> null
        }

        fun argument(
            hubAddress: String,
            incremental: Boolean,
            name: String,
            build: String,
            incrementalBuild: String,
        ): Map<String, Any> = mapOf(
            "project" to name.trim(),
            "build" to build.trim(),
            "incremental" to incremental,
            "incrementalBuild" to if (incremental) incrementalBuild.trim() else "",
            "hubAddress" to hubAddress.trim(),
        )
    }
}
