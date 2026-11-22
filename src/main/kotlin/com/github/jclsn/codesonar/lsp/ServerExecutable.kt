package com.github.jclsn.codesonar.lsp

import java.nio.file.Files
import java.nio.file.Path

/** Finds the codesonar-lsp executable. */
object ServerExecutable {
    const val DEFAULT = "codesonar-lsp"

    /** Replaces a leading `~` with the home directory. */
    fun expandHome(path: String, home: String = System.getProperty("user.home")): String =
        when {
            path == "~" -> home
            path.startsWith("~/") -> home + path.substring(1)
            else -> path
        }

    /**
     * The executable for the configured path or name. A name is looked up in
     * `$PATH` with [inPath], and then in `~/.cargo/bin`, where `cargo
     * install` puts the server: an IDE started from the desktop often does
     * not have it in its `$PATH`.
     */
    fun find(
        configured: String,
        home: String = System.getProperty("user.home"),
        inPath: (String) -> Path?,
    ): Path? {
        val name = expandHome(configured.trim().ifEmpty { DEFAULT }, home)

        if (name.contains('/') || name.contains('\\')) {
            return Path.of(name).takeIf { Files.isExecutable(it) }
        }

        return inPath(name)
            ?: Path.of(home, ".cargo", "bin", name).takeIf { Files.isExecutable(it) }
    }
}
