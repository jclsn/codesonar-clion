package com.github.jclsn.codesonar

/** The commands of codesonar-lsp, run with `workspace/executeCommand`. */
object Commands {
    const val CONNECT = "codesonar.connect"
    const val PROJECTS = "codesonar.projects"
    const val ANALYSES = "codesonar.analyses"
    const val DELETE_ANALYSIS = "codesonar.deleteAnalysis"
    const val UNCACHE = "codesonar.uncache"
    const val LOAD = "codesonar.load"
    const val HIDE = "codesonar.hide"
    const val STATUS = "codesonar.status"
    const val WARNINGS = "codesonar.warnings"
    const val ANALYZE_COMMAND = "codesonar.analyzeCommand"
    const val PROJECT = "codesonar.project"
    const val INIT_CONF = "codesonar.initConf"
    const val PRESETS = "codesonar.presets"
    const val ANALYSIS_OPTIONS = "codesonar.analysisOptions"
    const val CONFIGURE_ANALYSIS = "codesonar.configureAnalysis"
    const val EXPLAIN = "codesonar.explain"
    const val REFRESH = "codesonar.refresh"
}
