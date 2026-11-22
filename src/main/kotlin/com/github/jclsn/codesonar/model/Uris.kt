package com.github.jclsn.codesonar.model

import java.net.URI
import java.nio.file.Path

/** The path of a `file:` URI, or null. */
fun uriToPath(uri: String): String? = try {
    URI(uri).takeIf { it.scheme == "file" }?.let { Path.of(it).toString() }
} catch (_: Exception) {
    null
}

/** The file name of a URI. */
fun uriFileName(uri: String): String =
    (uriToPath(uri) ?: uri).substringAfterLast('/').substringAfterLast('\\')
