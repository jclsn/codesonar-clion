package com.github.jclsn.codesonar

import com.github.jclsn.codesonar.actions.CodeSonarEditorGroup
import com.github.jclsn.codesonar.lsp.CodeSonarLspIntegrationProvider
import com.github.jclsn.codesonar.model.Check
import com.github.jclsn.codesonar.model.Status
import com.github.jclsn.codesonar.model.WorkspaceProject
import com.github.jclsn.codesonar.settings.CodeSonarConfigurable
import com.github.jclsn.codesonar.settings.CodeSonarSettings
import com.github.jclsn.codesonar.ui.Choice
import com.github.jclsn.codesonar.ui.ChoiceDialog
import com.github.jclsn.codesonar.ui.CodeSonarStatusBarWidget
import com.github.jclsn.codesonar.ui.ConnectDialog
import com.github.jclsn.codesonar.ui.HtmlPopup
import com.github.jclsn.codesonar.ui.WarningsToolWindowFactory
import com.github.jclsn.codesonar.ui.WorkspaceConfDialog
import com.google.gson.JsonParser
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.ToolWindowEP
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/** Loads the plugin in a headless IDE: its descriptor, services and views. */
class PluginTest : BasePlatformTestCase() {
    private val actions = listOf(
        "Analyses", "Load", "LoadSarif", "Refresh", "Hide", "Explain", "OpenHub", "Warnings", "Status",
        "Analyze", "AnalyzeAs", "Conf", "Presets", "Checks", "Connect", "Projects",
    )

    fun `test registers the server for the platform`() {
        val providers = LspIntegrationProvider.EP_NAME.extensionList

        assertTrue(providers.any { it is CodeSonarLspIntegrationProvider })
    }

    fun `test registers the actions in the menus`() {
        val manager = ActionManager.getInstance()
        val group = manager.getAction("CodeSonar.Actions") as DefaultActionGroup
        val ids = group.getChildren(manager).mapNotNull { manager.getId(it) }

        assertEquals(actions.map { "CodeSonar.$it" }, ids)
        assertNotNull(manager.getAction("CodeSonar.ToolsMenu"))
        assertTrue(manager.getAction("CodeSonar.EditorPopupMenu") is CodeSonarEditorGroup)

        for (action in group.getChildren(manager).filter { manager.getId(it) != null }) {
            assertFalse(action.templatePresentation.text.isNullOrBlank())
            assertFalse(action.templatePresentation.description.isNullOrBlank())
        }
    }

    fun `test registers the views`() {
        assertTrue(StatusBarWidgetFactory.EP_NAME.extensionList.any { it.id == CodeSonarStatusBarWidget.ID })
        assertTrue(ToolWindowEP.EP_NAME.extensionList.any { it.id == WarningsToolWindowFactory.ID })
        assertNotNull(NotificationGroupManager.getInstance().getNotificationGroup(Notifier.GROUP))
    }

    fun `test reports that no server is running`() {
        val service = CodeSonarService.getInstance(project)
        var failed = false

        assertNull(service.client())
        service.execute(Commands.STATUS, failed = { failed = true }) { fail("no server is running") }
        assertTrue(failed)
    }

    fun `test keeps the status of the server and tells listeners`() {
        val service = CodeSonarService.getInstance(project)
        val events = mutableListOf<String>()

        project.messageBus.connect(testRootDisposable).subscribe(CodeSonarListener.TOPIC, object : CodeSonarListener {
            override fun loading(message: String) {
                events += message
            }

            override fun loaded(status: Status) {
                events += status.summary()
            }
        })

        service.onLoading(JsonParser.parseString("""{"analysis": 12}""").asJsonObject)
        assertEquals("CodeSonar: Loading analysis 12...", service.loading)

        service.onLoaded(
            JsonParser.parseString(
                """{"loaded": true, "name": "nightly", "total": 9, "shown": 7,
                    "counts": {"unchanged": 7, "moved": 0, "changed": 2, "unverified": 0}}""",
            ).asJsonObject,
        )
        assertNull(service.loading)
        assertEquals(7, service.status.shown)
        assertEquals(
            listOf("CodeSonar: Loading analysis 12...", "nightly: 7 of 9 warnings shown (7 unchanged, 2 changed)"),
            events,
        )
    }

    fun `test shows the analysis in the status bar widget`() {
        val service = CodeSonarService.getInstance(project)
        val widget = CodeSonarStatusBarWidget(project)
        Disposer.register(testRootDisposable, widget)

        // The project of the tests is shared, so its status is set first.
        service.onLoaded(JsonParser.parseString("""{"loaded": false}""").asJsonObject)
        assertEquals("CodeSonar", widget.getText())
        assertEquals("CodeSonar: No analysis loaded", widget.getTooltipText())

        service.onLoaded(JsonParser.parseString("""{"loaded": true, "name": "nightly", "total": 9, "shown": 7}""").asJsonObject)
        assertEquals("CodeSonar: 7/9", widget.getText())
        assertEquals("CodeSonar: nightly: 7 of 9 warnings shown", widget.getTooltipText())

        service.onLoading(JsonParser.parseString("""{"project": "test_project"}""").asJsonObject)
        assertEquals("CodeSonar: loading...", widget.getText())
        assertEquals("CodeSonar: Loading the newest analysis of test_project...", widget.getTooltipText())
        assertSame(widget, widget.getPresentation())
        assertNotNull(widget.getClickConsumer())
    }

    fun `test edits the settings`() {
        val settings = CodeSonarSettings.getInstance()
        val before = settings.state.copy()
        val configurable = CodeSonarConfigurable()

        try {
            assertNotNull(configurable.createComponent())
            configurable.reset()
            assertFalse(configurable.isModified)

            settings.state.username = "alice"
            assertTrue(configurable.isModified)
            assertEquals(
                JsonParser.parseString("""{"hub": {"username": "alice"}}"""),
                settings.initializationOptions(),
            )
        } finally {
            settings.loadState(before)
            configurable.disposeUIResources()
        }
    }

    fun `test builds the dialogs`() {
        val suggested = WorkspaceProject.Suggested("ws", "make -B", true, "", "hub:7340")
        val conf = WorkspaceConfDialog(project, WorkspaceProject("/ws/codesonar.conf", false, false, suggested))
        val connect = ConnectDialog(project)
        val checks = listOf(Choice(Check("Null Pointer Dereference", "C/C++", default = true, enabled = true), true))
        val choice = ChoiceDialog(project, "CodeSonar Checks", checks, { it.name }, { it.item.name })

        try {
            // The suggested incremental build lacks its command.
            assertEquals(
                "An incremental build needs an incremental build command",
                conf.doValidate()?.message,
            )
            assertEquals(true, conf.argument()["incremental"])
            assertEquals(emptyMap<String, String>(), connect.argument())
        } finally {
            listOf(conf, connect, choice).forEach { it.close(DialogWrapper.CANCEL_EXIT_CODE) }
        }
    }

    fun `test takes the colors of the theme`() {
        val palette = HtmlPopup.palette()

        for (color in listOf(palette.accent, palette.changed, palette.muted, palette.unchanged, palette.moved)) {
            assertTrue(color, Regex("#[0-9a-fA-F]{6}").matches(color))
        }
    }
}
