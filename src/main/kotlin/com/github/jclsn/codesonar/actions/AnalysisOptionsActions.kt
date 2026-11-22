package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.AnalysisOptions
import com.github.jclsn.codesonar.model.Check
import com.github.jclsn.codesonar.model.Preset
import com.github.jclsn.codesonar.ui.Choice
import com.github.jclsn.codesonar.ui.ChoiceDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

/**
 * Chooses the CodeSonar presets of analyses, e.g. `misrac2025` for the
 * rules of MISRA C:2025. The workspace configuration keeps them.
 */
class PresetsAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.PRESETS) { result ->
            val choices = Preset.parseList(result).map { Choice(it, it.selected) }

            if (choices.isEmpty()) {
                Notifier.error(project, "The CodeSonar installation has no presets")
                return@execute
            }

            val dialog = ChoiceDialog(
                project,
                "CodeSonar Presets",
                choices,
                searchText = { "${it.name} ${it.description.orEmpty()}" },
                label = { label(it.item) },
            )

            if (dialog.showAndGet()) {
                val names = choices.filter { it.ticked }.map { it.item.name }

                service.execute(Commands.CONFIGURE_ANALYSIS, mapOf("presets" to names)) {
                    Notifier.info(project, saved(names, AnalysisOptions.parse(it).conf))
                }
            }
        }
    }

    companion object {
        fun label(preset: Preset): String =
            if (preset.description.isNullOrBlank()) preset.name else "${preset.name}  –  ${preset.description}"

        fun saved(names: List<String>, conf: String): String {
            val file = conf.substringAfterLast('/')

            return if (names.isEmpty()) {
                "Analyses use no presets ($file)"
            } else {
                "Analyses use the presets ${names.joinToString(", ")} ($file)"
            }
        }
    }
}

/**
 * Chooses the checks (warning classes) of analyses. The workspace
 * configuration keeps rules for those that differ from CodeSonar's default.
 */
class ChecksAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.ANALYSIS_OPTIONS) { result ->
            val checks = AnalysisOptions.parse(result).checks
            val choices = checks.map { Choice(it, it.enabled) }

            if (choices.isEmpty()) {
                Notifier.error(project, "The CodeSonar installation has no checks")
                return@execute
            }

            val languages = checks.map { it.language }.filter { it.isNotEmpty() }.distinct().sorted()
            val dialog = ChoiceDialog(
                project,
                "CodeSonar Checks (${languages.joinToString(", ")})",
                choices,
                searchText = { it.name },
                label = { label(it.item, it.ticked) },
            )

            if (dialog.showAndGet()) {
                service.execute(Commands.CONFIGURE_ANALYSIS, mapOf("checks" to changed(choices))) {
                    val file = AnalysisOptions.parse(it).conf.substringAfterLast('/')
                    Notifier.info(project, "Saved the checks in $file")
                }
            }
        }
    }

    companion object {
        /** Checks that differ from CodeSonar's default are marked. */
        fun label(check: Check, ticked: Boolean): String =
            if (ticked != check.default) "${check.name}  (changed)" else check.name

        /** The checks whose tick changed, as `codesonar.configureAnalysis` takes them. */
        fun changed(choices: List<Choice<Check>>): List<Map<String, Any>> =
            choices.filter { it.ticked != it.item.enabled }
                .map { mapOf("class" to it.item.name, "enabled" to it.ticked) }
    }
}
