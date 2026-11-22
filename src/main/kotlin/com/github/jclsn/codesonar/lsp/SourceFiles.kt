package com.github.jclsn.codesonar.lsp

/** The files the server is started for: C and C++ sources and headers. */
object SourceFiles {
    private val EXTENSIONS = setOf(
        "c", "h",
        "cc", "cpp", "cxx", "c++", "cppm", "ixx",
        "hh", "hpp", "hxx", "h++",
        "inl", "ipp", "tcc", "tpp",
    )

    fun isSource(fileName: String): Boolean =
        fileName.substringAfterLast('.', "").lowercase() in EXTENSIONS
}
