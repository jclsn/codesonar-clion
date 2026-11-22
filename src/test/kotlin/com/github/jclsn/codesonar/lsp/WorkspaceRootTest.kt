package com.github.jclsn.codesonar.lsp

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkspaceRootTest {
    @TempDir
    lateinit var dir: Path

    private fun source(vararg parts: String): Path {
        val file = parts.fold(dir) { path, part -> path.resolve(part) }
        Files.createDirectories(file.parent)
        return Files.createFile(file)
    }

    @Test
    fun `finds the directory with a codesonar conf`() {
        val file = source("project", "src", "main.c")
        Files.createFile(dir.resolve("project").resolve("codesonar.conf"))

        assertEquals(dir.resolve("project"), WorkspaceRoot.find(file))
    }

    @Test
    fun `finds the directory with project files or a repository`() {
        val analyzed = source("analyzed", "main.c")
        val cloned = source("cloned", "lib", "util.c")
        Files.createDirectory(dir.resolve("analyzed").resolve(".codesonar"))
        Files.createDirectory(dir.resolve("cloned").resolve(".git"))

        assertEquals(dir.resolve("analyzed"), WorkspaceRoot.find(analyzed))
        assertEquals(dir.resolve("cloned"), WorkspaceRoot.find(cloned))
    }

    @Test
    fun `takes the closest directory`() {
        val file = source("outer", "inner", "main.c")
        Files.createDirectory(dir.resolve("outer").resolve(".git"))
        Files.createFile(dir.resolve("outer").resolve("inner").resolve("codesonar.conf"))

        assertEquals(dir.resolve("outer").resolve("inner"), WorkspaceRoot.find(file))
    }

    @Test
    fun `finds nothing without a marker`() {
        // The temporary directory may be below a marker of the machine.
        val file = Path.of("/nonexistent-codesonar-root/src/main.c")

        assertNull(WorkspaceRoot.find(file))
    }
}
