package com.github.jclsn.codesonar.lsp

import java.nio.file.Files
import java.nio.file.Path

/** Finds the workspace of a file, which the server is started for. */
object WorkspaceRoot {
    /** What marks the root of a workspace. */
    val MARKERS = listOf("codesonar.conf", ".codesonar", ".git")

    /** The closest directory above [file] with one of the [MARKERS]. */
    fun find(file: Path): Path? =
        generateSequence(file.toAbsolutePath().parent) { it.parent }
            .firstOrNull { dir -> MARKERS.any { Files.exists(dir.resolve(it)) } }
}
