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

/**
 * Whether a link may be opened in a browser: only `http` and `https`.
 * Links come from analyses and the hub, and other schemes, like `file`,
 * can start other programs.
 */
fun isWebLink(url: String): Boolean = try {
    URI(url.trim()).let { it.scheme?.lowercase() in setOf("http", "https") && !it.host.isNullOrEmpty() }
} catch (_: Exception) {
    false
}
