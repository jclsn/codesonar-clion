package com.github.jclsn.codesonar

import com.github.jclsn.codesonar.actions.ToggleAction
import com.github.jclsn.codesonar.lsp.CodeSonarLanguageClient
import com.github.jclsn.codesonar.model.DisableCheckArgument
import com.github.jclsn.codesonar.model.DisableCheckResult
import com.github.jclsn.codesonar.model.Explanation
import com.github.jclsn.codesonar.model.Json
import com.github.jclsn.codesonar.model.Resolution
import com.github.jclsn.codesonar.model.ResolveArgument
import com.github.jclsn.codesonar.model.Status
import com.github.jclsn.codesonar.model.Warning
import com.github.jclsn.codesonar.model.WorkspaceProject
import com.github.jclsn.codesonar.settings.CodeSonarSettings
import com.github.jclsn.codesonar.settings.InitializationOptions
import com.github.jclsn.codesonar.ui.ExplanationHtml
import com.github.jclsn.codesonar.ui.Palette
import com.github.jclsn.codesonar.ui.WorkspaceConfDialog
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Talks to the real server like the IDE does: with lsp4j, the client of
 * the plugin and the settings it builds. Runs with codesonar-lsp in
 * `$CODESONAR_LSP` or `$PATH` and is skipped without it.
 */
class ServerProtocolTest {
    @TempDir
    lateinit var dir: Path

    private lateinit var workspace: Path
    private lateinit var process: Process
    private lateinit var server: LanguageServer
    private val loading = LinkedBlockingQueue<JsonObject>()
    private val loaded = LinkedBlockingQueue<JsonObject>()

    @BeforeEach
    fun start() {
        val executable = executable()
        assumeTrue(executable != null, "codesonar-lsp not found, set CODESONAR_LSP")

        workspace = Files.createDirectory(dir.resolve("demo"))
        for (name in listOf("main.c", "codesonar.sarif")) {
            javaClass.getResourceAsStream("/demo/$name")!!.use { Files.copy(it, workspace.resolve(name)) }
        }

        // Keeps the memory and the cache of the tests away from the user's.
        process = ProcessBuilder(executable.toString())
            .directory(workspace.toFile())
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .apply {
                environment()["XDG_STATE_HOME"] = dir.resolve("state").toString()
                environment()["XDG_CACHE_HOME"] = dir.resolve("cache").toString()
            }
            .start()

        val client = CodeSonarLanguageClient(notificationsHandler(), object : CodeSonarLanguageClient.Listener {
            override fun loading(params: JsonObject) {
                loading.add(params)
            }

            override fun loaded(params: JsonObject) {
                loaded.add(params)
            }
        })
        val launcher = Launcher.Builder<LanguageServer>()
            .setLocalService(client)
            .setRemoteInterface(LanguageServer::class.java)
            .setInput(process.inputStream)
            .setOutput(process.outputStream)
            .create()

        launcher.startListening()
        server = launcher.remoteProxy

        val state = CodeSonarSettings.State(sarifFile = "codesonar.sarif")
        val params = InitializeParams().apply {
            capabilities = ClientCapabilities()
            workspaceFolders = listOf(WorkspaceFolder(workspace.toUri().toString(), "demo"))
            initializationOptions = InitializationOptions.build(state, null)
        }

        server.initialize(params).get(TIMEOUT, TimeUnit.SECONDS)
        server.initialized(InitializedParams())
    }

    @AfterEach
    fun stop() {
        if (::process.isInitialized) {
            process.destroyForcibly()
        }
    }

    private fun executable(): Path? {
        val configured = System.getenv("CODESONAR_LSP")?.takeIf { it.isNotBlank() }?.let(Path::of)
        val candidates = listOfNotNull(configured) + System.getenv("PATH").orEmpty()
            .split(java.io.File.pathSeparator)
            .filter { it.isNotEmpty() }
            .map { Path.of(it, "codesonar-lsp") }

        return candidates.firstOrNull { Files.isExecutable(it) }
    }

    /** The platform's side of the client, which answers everything with nothing. */
    private fun notificationsHandler(): LspServerNotificationsHandler = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(LspServerNotificationsHandler::class.java),
    ) { _, method, _ ->
        if (method.returnType == CompletableFuture::class.java) CompletableFuture.completedFuture(null) else null
    } as LspServerNotificationsHandler

    private fun execute(command: String, argument: Any? = null): JsonElement {
        val params = ExecuteCommandParams(command, listOfNotNull(argument))

        return Json.tree(server.workspaceService.executeCommand(params).get(TIMEOUT, TimeUnit.SECONDS))
    }

    private fun awaitLoaded(): Status {
        val params = assertNotNull(loaded.poll(TIMEOUT, TimeUnit.SECONDS), "no codesonar/loaded")

        return Status.parse(params)
    }

    @Test
    fun `announces and shows the sarif file of the settings`() {
        val announced = assertNotNull(loading.poll(TIMEOUT, TimeUnit.SECONDS), "no codesonar/loading")
        val message = CodeSonarService.loadingMessage(announced, workspace.toString())
        val status = awaitLoaded()

        assertEquals("CodeSonar: Loading codesonar.sarif...", message)
        assertTrue(status.loaded)
        assertEquals(12, status.total)
        assertEquals(12, status.shown)
        // Without a hub, nothing tells whether the code is the analyzed one.
        assertEquals("codesonar.sarif: 12 of 12 warnings shown (12 unverified)", status.summary())
    }

    @Test
    fun `reads the status and the warnings`() {
        awaitLoaded()

        val status = Status.parse(execute(Commands.STATUS))
        val warnings = Warning.parseList(execute(Commands.WARNINGS))

        assertTrue(status.loaded)
        assertFalse(status.hidden)
        assertEquals(12, warnings.size)
        assertEquals(setOf(workspace.resolve("main.c").toString()), warnings.map { it.path }.toSet())
        assertContains(warnings.map { it.code }, "LANG.MEM.NPD")
        assertTrue(warnings.all { it.message.isNotEmpty() && it.status == "unverified" })
    }

    @Test
    fun `explains the warnings of a line`() {
        awaitLoaded()

        val warning = Warning.parseList(execute(Commands.WARNINGS)).first { it.code == "LANG.ARITH.DIVZERO" }
        val argument = mapOf(
            "uri" to warning.uri,
            "range" to mapOf(
                "start" to mapOf("line" to warning.line, "character" to 0),
                "end" to mapOf("line" to warning.line + 1, "character" to 0),
            ),
        )
        val explanations = Explanation.parseList(execute(Commands.EXPLAIN, argument))
        val html = ExplanationHtml.render(explanations, Palette("#1", "#2", "#3", "#4", "#5"))

        assertEquals(listOf("LANG.ARITH.DIVZERO"), explanations.map { it.ruleId })
        assertTrue(explanations[0].message.isNotEmpty())
        assertNotNull(explanations[0].description)
        assertContains(html, "LANG.ARITH.DIVZERO")
    }

    @Test
    fun `refuses to resolve warnings without a hub`() {
        awaitLoaded()

        val warning = Warning.parseList(execute(Commands.WARNINGS)).first()
        val argument = ResolveArgument.quiet(ResolveArgument.line(warning.uri, warning.line, Resolution.FIXED))
        val error = assertFailsWith<ExecutionException> { execute(Commands.RESOLVE, argument) }

        assertContains(CodeSonarService.errorMessage(Commands.RESOLVE, error), "no hub")
        assertEquals(12, Status.parse(execute(Commands.STATUS)).shown)
    }

    @Test
    fun `tells the outcome of disabling a check`() {
        awaitLoaded()

        val warning = Warning.parseList(execute(Commands.WARNINGS)).first()
        val argument = ResolveArgument.quiet(DisableCheckArgument.of(warning.code))
        val result = DisableCheckResult.parse(execute(Commands.DISABLE_CHECK, argument))

        assertContains(result.message, warning.code)
    }

    @Test
    fun `hides and shows the analysis`() {
        awaitLoaded()

        val hidden = Status.parse(execute(Commands.HIDE))
        assertTrue(hidden.hidden)
        assertEquals("Hiding the analysis...", hidden.summary())
        assertEquals(emptyList(), Warning.parseList(execute(Commands.WARNINGS)))

        val shown = Status.parse(execute(Commands.LOAD))
        assertFalse(shown.hidden)
        assertEquals(12, shown.shown)
    }

    @Test
    fun `toggles the analysis`() {
        awaitLoaded()

        val hidden = Status.parse(execute(Commands.TOGGLE))
        assertTrue(hidden.hidden)
        assertEquals("Show Analysis", ToggleAction.text(hidden.hidden))
        assertEquals(emptyList(), Warning.parseList(execute(Commands.WARNINGS)))

        val shown = Status.parse(execute(Commands.TOGGLE))
        assertFalse(shown.hidden)
        assertEquals("Hide Analysis", ToggleAction.text(shown.hidden))
        assertEquals(12, shown.shown)
    }

    @Test
    fun `starts the workspace configuration`() {
        awaitLoaded()

        val before = assertNotNull(WorkspaceProject.parse(execute(Commands.PROJECT)))
        assertFalse(before.section)
        assertFalse(before.workspaceConfExists)
        assertEquals("No codesonar.conf found.", WorkspaceConfDialog.missing(before))
        assertEquals("demo", before.suggested.project)
        assertEquals("make -B", before.suggested.build)
        assertTrue(before.suggested.incremental)

        val argument = WorkspaceConfDialog.argument("hub.example.com:7340", true, "demo", "make -B", "make")
        val after = assertNotNull(WorkspaceProject.parse(execute(Commands.INIT_CONF, argument)))
        val conf = Files.readString(Path.of(after.workspaceConf))

        assertTrue(after.section)
        assertEquals(workspace.resolve("codesonar.conf").toString(), after.workspaceConf)
        assertContains(conf, "# codesonar-lsp: project = ")
        assertContains(conf, "HUB_ADDRESS = hub.example.com:7340")
        assertContains(conf, "INCREMENTAL_BUILD = Yes")
    }

    @Test
    fun `explains failed commands`() {
        awaitLoaded()

        val error = try {
            execute(Commands.ANALYSES)
            null
        } catch (error: ExecutionException) {
            error
        }
        val message = CodeSonarService.errorMessage(Commands.ANALYSES, assertNotNull(error, "no error without a hub"))

        assertTrue(message.isNotBlank())
        assertFalse(message.startsWith("org.eclipse"), message)
    }

    private companion object {
        const val TIMEOUT = 20L
    }
}
