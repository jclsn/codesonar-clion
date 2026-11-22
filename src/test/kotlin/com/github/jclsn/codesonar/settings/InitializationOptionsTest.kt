package com.github.jclsn.codesonar.settings

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InitializationOptionsTest {
    private fun options(state: CodeSonarSettings.State, password: String? = null) =
        InitializationOptions.build(state, password)

    private fun json(text: String) = JsonParser.parseString(text)

    @Test
    fun `is empty without settings`() {
        assertEquals(json("{}"), options(CodeSonarSettings.State()))
    }

    @Test
    fun `passes the hub and the credentials`() {
        val state = CodeSonarSettings.State(
            hubUrl = " https://hub.example.com:7340 ",
            username = "alice",
            passwordFile = "~/.config/codesonar/password",
            hubProject = "my_project",
        )

        assertEquals(
            json(
                """{"hub": {"url": "https://hub.example.com:7340", "username": "alice",
                   "passwordFile": "~/.config/codesonar/password", "project": "my_project"}}""",
            ),
            options(state),
        )
        assertEquals(
            json("""{"hub": {"username": "alice", "password": "secret"}}"""),
            options(CodeSonarSettings.State(username = "alice"), "secret"),
        )
    }

    @Test
    fun `passes the sarif file and marks changed warnings`() {
        val state = CodeSonarSettings.State(sarifFile = "codesonar.sarif", markChanged = true)

        assertEquals(json("""{"sarifFile": "codesonar.sarif", "changed": "mark"}"""), options(state))
    }

    @Test
    fun `adds the fields to the additional settings`() {
        val state = CodeSonarSettings.State(
            hubUrl = "http://hub:7340",
            additionalSettings = """{"cachedAnalyses": 5, "hub": {"url": "http://other", "acceptInvalidCerts": true},
                "analyze": {"buildCommand": "make -B"}}""",
        )

        assertEquals(
            json(
                """{"cachedAnalyses": 5, "hub": {"url": "http://hub:7340", "acceptInvalidCerts": true},
                   "analyze": {"buildCommand": "make -B"}}""",
            ),
            options(state),
        )
    }

    @Test
    fun `ignores additional settings that are not an object`() {
        assertNull(InitializationOptions.parseAdditional("[1, 2]"))
        assertNull(InitializationOptions.parseAdditional("{broken"))
        assertNotNull(InitializationOptions.parseAdditional("  "))
        assertEquals(json("{}"), options(CodeSonarSettings.State(additionalSettings = "{broken")))
    }
}
