package com.github.jclsn.codesonar

import com.google.gson.JsonParser
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import java.util.concurrent.CompletionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CodeSonarServiceTest {
    private fun loading(json: String) =
        CodeSonarService.loadingMessage(JsonParser.parseString(json).asJsonObject, "/ws")

    @Test
    fun `announces what is loading`() {
        assertEquals("CodeSonar: Loading analysis 12...", loading("""{"analysis": 12}"""))
        assertEquals("CodeSonar: Loading analysis 12...", loading("""{"analysis": "12"}"""))
        assertEquals("CodeSonar: Loading codesonar.sarif...", loading("""{"file": "/ws/codesonar.sarif"}"""))
        assertEquals("CodeSonar: Loading /other/a.sarif...", loading("""{"file": "/other/a.sarif"}"""))
        assertEquals(
            "CodeSonar: Loading the newest analysis of test_project...",
            loading("""{"project": "test_project"}"""),
        )
        assertNull(loading("""{"project": null}"""))
    }

    @Test
    fun `takes the message of the server for failed commands`() {
        val response = ResponseErrorException(ResponseError(-32603, "no hub is configured", null))

        assertEquals("no hub is configured", CodeSonarService.errorMessage("codesonar.analyses", response))
        assertEquals(
            "no hub is configured",
            CodeSonarService.errorMessage("codesonar.analyses", CompletionException(response)),
        )
        assertEquals("timeout", CodeSonarService.errorMessage("codesonar.load", RuntimeException("timeout")))
        assertEquals("codesonar.load failed", CodeSonarService.errorMessage("codesonar.load", null))
    }

    @Test
    fun `starts errors with a capital letter`() {
        assertEquals("No hub is configured", Notifier.capitalize("no hub is configured"))
        assertEquals("Analysis failed", Notifier.capitalize("Analysis failed"))
    }
}
