package com.github.jclsn.codesonar.lsp

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerExecutableTest {
    @TempDir
    lateinit var home: Path

    private fun executable(vararg parts: String): Path {
        val file = parts.fold(home) { path, part -> path.resolve(part) }
        Files.createDirectories(file.parent)
        Files.createFile(file)
        return Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"))
    }

    private fun find(configured: String, inPath: (String) -> Path? = { null }) =
        ServerExecutable.find(configured, home.toString(), inPath)

    @Test
    fun `expands the home directory`() {
        assertEquals("/home/alice/bin/codesonar-lsp", ServerExecutable.expandHome("~/bin/codesonar-lsp", "/home/alice"))
        assertEquals("/home/alice", ServerExecutable.expandHome("~", "/home/alice"))
        assertEquals("/opt/~/server", ServerExecutable.expandHome("/opt/~/server", "/home/alice"))
    }

    @Test
    fun `takes a path as it is`() {
        val server = executable("tools", "codesonar-lsp")

        assertEquals(server, find(server.toString()))
        assertEquals(server, find("~/tools/codesonar-lsp"))
        assertNull(find("$home/tools/missing"))
    }

    @Test
    fun `looks a name up in the path`() {
        val server = executable("bin", "codesonar-lsp")

        assertEquals(server, find("codesonar-lsp") { name -> server.takeIf { name == "codesonar-lsp" } })
    }

    @Test
    fun `falls back to the binaries of cargo`() {
        val server = executable(".cargo", "bin", "codesonar-lsp")

        assertEquals(server, find("codesonar-lsp"))
        assertEquals(server, find("  "))
        assertNull(find("other-server"))
    }
}
